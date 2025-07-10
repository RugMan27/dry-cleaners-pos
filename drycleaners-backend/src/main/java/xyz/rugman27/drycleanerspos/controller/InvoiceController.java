package xyz.rugman27.drycleanerspos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.rugman27.drycleanerspos.data.AuditAction;
import xyz.rugman27.drycleanerspos.dto.*;
import xyz.rugman27.drycleanerspos.mapper.CustomerMapper;
import xyz.rugman27.drycleanerspos.mapper.InvoiceMapper;
import xyz.rugman27.drycleanerspos.model.CustomerModel;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;
import xyz.rugman27.drycleanerspos.model.InvoiceModel;
import xyz.rugman27.drycleanerspos.service.AuditLogService;
import xyz.rugman27.drycleanerspos.service.CustomerService;
import xyz.rugman27.drycleanerspos.service.EmployeeService;
import xyz.rugman27.drycleanerspos.service.InvoiceService;
import xyz.rugman27.drycleanerspos.utilites.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices/cleaning")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private EmployeeService employeeService;


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK')")
    @PostMapping("/create")
    public ResponseEntity<? > create(HttpServletRequest request, @RequestBody String requestBody) {
        InvoiceRequest invoiceRequest = JsonUtils.fromJson(requestBody, InvoiceRequest.class);

        if(Utils.notBlankString(invoiceRequest.getCustomerName())){
            InvoiceDto invoiceDto = invoiceService.createNewCleaningInvoice(invoiceRequest);

            InvoiceModel invoiceModel = InvoiceMapper.toModel(invoiceDto);
            invoiceService.save(invoiceModel);

            auditLogService.log(request, AuditAction.CREATE_INVOICE, invoiceDto.getId(), JsonUtils.toJson(invoiceModel));
            return ApiResponse.success(HttpStatus.OK, "Invoice created successfully","/api/invoices/cleaning/create", InvoiceMapper.toDto(invoiceModel));
        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "No name provided",
                    "/api/invoices/cleaning/create"
            );
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK')")
    @PutMapping("/update")
    public ResponseEntity<?> update(HttpServletRequest request, @RequestBody String requestBody,@RequestParam String id) {
        if(Utils.notBlankString(id)){
            InvoiceRequest invoiceRequest = JsonUtils.fromJson(requestBody, InvoiceRequest.class);

            InvoiceModel invoiceModel = invoiceService.getById(id);
            if(invoiceModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "Invoice '"+id+"' not found",
                        "/api/invoices/cleaning/update?id=" + id
                );
            }
            InvoiceDto invoiceDto = new InvoiceDto(invoiceModel.getId(), invoiceModel.getServiceType(), invoiceModel.getDateCreated());

            InvoiceMapper.updateFromRequest(invoiceDto, invoiceRequest);

            invoiceService.save(InvoiceMapper.toModel(invoiceDto));

            auditLogService.log(request, AuditAction.UPDATE_INVOICE, invoiceDto.getId(), JsonUtils.toJson(invoiceDto));
            return ApiResponse.success(HttpStatus.OK, "Invoice updated successfully","/api/invoices/cleaning/update?id=" +id, invoiceDto);
        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank id",
                    "/api/invoices/cleaning/update?id=" + id
            );
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK')")
    @PutMapping("/update-payment")
    public ResponseEntity<?> updatePayment(HttpServletRequest request, @RequestBody String requestBody, @RequestParam String id) {
        if (!Utils.notBlankString(id)) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Blank ID", "/api/invoices/cleaning/update-payment?id=" + id);
        }

        InvoiceModel invoiceModel = invoiceService.getById(id);
        if (invoiceModel == null) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Invoice '" + id + "' not found", "/api/invoices/cleaning/update-payment?id=" + id);
        }

        EmployeeDto requestingEmployeeDto = AuthUtils.getEmployeeDto(request, employeeService);
        InvoiceDto invoiceDto = InvoiceMapper.toDto(invoiceService.getById(id));
        InvoiceDto.PaymentInfo paymentInfo = JsonUtils.fromJson(requestBody, InvoiceDto.PaymentInfo.class);
        InvoiceDto.PaymentStatus currentStatus = invoiceModel.getPaymentStatus();
        boolean isClerk = JwtUtils.getEmployeeTypeFromClaims(Objects.requireNonNull(JwtUtils.extractClaimsFromRequest(request))) ==  EmployeeModel.EmployeeType.CLERK;
        long now = System.currentTimeMillis();
        long allowedDrift = 2 * 60 * 1000; // 2 minutes in milliseconds
        long paymentDate = paymentInfo.getPaymentDate();
        String logMessage = "";



        // Disallow changing payment if already marked PAID
        if (currentStatus == InvoiceDto.PaymentStatus.PAID) {
            if(
                    requestingEmployeeDto.getEmployeeType() != EmployeeModel.EmployeeType.MANAGER &&
                    requestingEmployeeDto.getEmployeeType() != EmployeeModel.EmployeeType.ADMIN &&
                    requestingEmployeeDto.getEmployeeType() != EmployeeModel.EmployeeType.OWNER
            ) {
                return ApiResponse.error(HttpStatus.BAD_REQUEST, "Invoice is already marked as PAID and cannot be modified", "/api/invoices/cleaning/update-payment?id=" + id);
            }
        }

        // Validate combination of fields
        boolean hasDate = paymentDate != 0L;
        boolean hasMethod = paymentInfo.getPaymentMethod() != null;

        // Validate bad states
        if (hasDate && !hasMethod) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Payment date set but payment method is missing", "/api/invoices/cleaning/update-payment?id=" + id);
        }

        if (!hasDate && hasMethod && paymentInfo.isPaid()) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Paid is true but no payment date provided", "/api/invoices/cleaning/update-payment?id=" + id);
        }

        // Auto-mark as paid if date+method valid and method not ACCOUNT
        if (hasDate && hasMethod) {
            if (Math.abs(now - paymentDate) > allowedDrift) {
                if (isClerk) {
                    return ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Invalid payment time: Payment date must be within 2 minutes of current time.",
                            "/api/invoices/cleaning/update-payment?id=" + id
                    );
                } else  {
                    logMessage += " [Override] Payment time drift (" + (now - paymentDate) + "ms) allowed by privileged user. ";
                }
            }
            if (paymentInfo.getPaymentMethod() != InvoiceDto.PaymentMethod.ACCOUNT) {
                paymentInfo.setPaid(true);
                invoiceDto.setPaymentStatus(InvoiceDto.PaymentStatus.PAID);
                logMessage += "Invoice set as PAID based on method and date.";
                if (invoiceDto.checkManagerAttention()){
                    logMessage += " Invoice PAID without manager price approval!";
                }

            } else {
                invoiceDto.setPaymentStatus(InvoiceDto.PaymentStatus.ACCOUNT);
                paymentInfo.setPaid(false); // ACCOUNT isn't paid immediately
                logMessage += "Invoice set to ACCOUNT status.";
            }
        } else {
            invoiceDto.setPaymentStatus(InvoiceDto.PaymentStatus.UNPAID);
            logMessage = "Invoice marked as unpaid (incomplete payment info).";
        }

        invoiceDto.setPaymentInfo(paymentInfo);

        invoiceService.save(InvoiceMapper.toModel(invoiceDto));

        auditLogService.log(request, AuditAction.UPDATE_INVOICE_PAYMENT, invoiceDto.getId(), logMessage + "\n" + JsonUtils.toJson(invoiceDto));

        return ApiResponse.success(HttpStatus.OK, "Invoice updated successfully", "/api/invoices/cleaning/update?id=" + id, invoiceDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK', 'EMPLOYEE')")
    @GetMapping
    public ResponseEntity<?> getALl(HttpServletRequest request) {
        List<InvoiceDto> invoiceDtos = InvoiceMapper.toDto(invoiceService.getAll());
        List<String> ids = invoiceDtos.stream()
                .map(InvoiceDto::getId)
                .collect(Collectors.toList());
        auditLogService.log(request, AuditAction.GET_INVOICES, String.valueOf(invoiceDtos.size()), JsonUtils.toJson(ids));
        return ApiResponse.success(HttpStatus.OK,"Got all Customers.", "/api/invoices/cleaning", invoiceDtos);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK', 'EMPLOYEE')")
    @GetMapping("/search")
    public ResponseEntity<?> searchInvoices(
            HttpServletRequest request,
            @ModelAttribute InvoiceSearchRequest searchRequest
    ) {
        List<InvoiceDto> invoiceDtos = InvoiceMapper.toDto(
                invoiceService.searchInvoices(searchRequest)
        );

        List<String> ids = invoiceDtos.stream()
                .map(InvoiceDto::getId)
                .collect(Collectors.toList());

        auditLogService.log(request, AuditAction.GET_INVOICES,
                String.valueOf(invoiceDtos.size()), JsonUtils.toJson(ids));

        return ApiResponse.success(HttpStatus.OK, "Search results", "/api/invoices/cleaning/search", invoiceDtos);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            HttpServletRequest request,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) Integer pastDays
    ) {

        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();

        searchRequest.setPaymentStatus(InvoiceDto.PaymentStatus.PAID);

        HashMap<String, Object> data = new HashMap<>();

        if(fromDate != null && toDate != null)  {

            Map<String, Object> total = getRevenueForDate(fromDate, toDate);

            data.putAll(total);

        }else if(pastDays != null) {

            List<Map<String, Object>> revenueArray = new ArrayList<>();

            LocalDate endDate = LocalDate.now();
            List<LocalDate> dateRange = new ArrayList<>();
            for (LocalDate date = endDate.minusDays(pastDays); !date.isAfter(endDate); date = date.plusDays(1)) {
                dateRange.add(date);
            }

            double totalRevenue = 0.0;
            int totalCount = 0;

            for (LocalDate date : dateRange) {
                Map<String, Object> rawDayRevenue = getRevenueForDate(date, date);

                HashMap<String, Object> dayRevenue = new HashMap<>();

                Double revenue = (Double) rawDayRevenue.get("total");
                int count = (Integer) rawDayRevenue.get("count");

                dayRevenue.put("name", date);
                dayRevenue.put("revenue", revenue);
                dayRevenue.put("invoice_count", count);

                totalRevenue += revenue;
                totalCount += count;

                revenueArray.add(dayRevenue);

            }

            data.put("total_revenue", totalRevenue);
            data.put("total_count", totalCount);
            data.put("revenues", revenueArray);

        }else {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Must include to and from date OR past days", "/api/invoices/cleaning/revenue");
        }

        auditLogService.log(request, AuditAction.GET_INVOICES,
                String.valueOf(Objects.requireNonNullElse(data.get("count"), data.get("total_count"))), JsonUtils.toJson(data));

        return ApiResponse.success(HttpStatus.OK, "Revenue results", "/api/invoices/cleaning/revenue", data);
    }

    private Map<String, Object> getRevenueForDate(LocalDate from, LocalDate to) {
        InvoiceSearchRequest searchRequest = new InvoiceSearchRequest();
        searchRequest.setPaymentStatus(InvoiceDto.PaymentStatus.PAID);
        searchRequest.setPaymentFromDate(from);
        searchRequest.setPaymentToDate(to);

        List<InvoiceDto> invoiceDtos = InvoiceMapper.toDto(
                invoiceService.searchInvoices(searchRequest)
        );



        double sum = 0.0;
        for (InvoiceDto invoiceDto : invoiceDtos) {
            sum += invoiceDto.getPaymentInfo().getTotal();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", sum);
        result.put("count", invoiceDtos.size());

        return result;
    }




    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(HttpServletRequest request, @RequestParam String id){
        if(invoiceService.deleteByID(id)){
            auditLogService.log(request, AuditAction.DELETE_INVOICE, id, null);
            return ApiResponse.success(HttpStatus.OK,"Deleted invoice '"+id+"'", "/api/invoices/cleaning/delete?id=" + id, null);
        }
        return ApiResponse.error(HttpStatus.NOT_FOUND,"Could not delete invoice '"+id+"'", "/api/invoices/cleaning/delete?id=" + id);
    }






}
