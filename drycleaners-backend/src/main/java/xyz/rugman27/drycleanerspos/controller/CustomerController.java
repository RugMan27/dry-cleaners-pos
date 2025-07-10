package xyz.rugman27.drycleanerspos.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.rugman27.drycleanerspos.data.AuditAction;
import xyz.rugman27.drycleanerspos.dto.CustomerDto;
import xyz.rugman27.drycleanerspos.dto.CustomerRequest;
import xyz.rugman27.drycleanerspos.dto.CustomerSearchRequest;
import xyz.rugman27.drycleanerspos.mapper.CustomerMapper;
import xyz.rugman27.drycleanerspos.model.CustomerModel;
import xyz.rugman27.drycleanerspos.repository.AuditLogRepository;
import xyz.rugman27.drycleanerspos.service.AuditLogService;
import xyz.rugman27.drycleanerspos.service.CustomerService;
import xyz.rugman27.drycleanerspos.utilites.ApiResponse;
import xyz.rugman27.drycleanerspos.utilites.JsonUtils;
import xyz.rugman27.drycleanerspos.utilites.Utils;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {


    @Autowired
    private CustomerService customerService;
    @Autowired
    private AuditLogService auditLogService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK')")
    @PostMapping("/create")
    public ResponseEntity<? > create(HttpServletRequest request, @RequestBody String requestBody) {
        CustomerRequest customerRequest = JsonUtils.fromJson(requestBody, CustomerRequest.class);

        if(Utils.notBlankString(customerRequest.getLastName())){
            CustomerModel customerModel = customerService.createCustomerFromLastName(customerRequest.getLastName());
            CustomerDto customerDto = CustomerMapper.toDto(customerModel);
            CustomerMapper.updateFromRequest(customerDto, customerRequest);

            customerService.saveCustomer(CustomerMapper.toModel(customerDto));
            auditLogService.log(request, AuditAction.CREATE_CUSTOMER, customerDto.getId(), JsonUtils.toJson(customerDto));
            return ApiResponse.success(HttpStatus.OK, "Customer created successfully","/api/customers/create", customerDto);
        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "No last name provided",
                    "/api/customers/create"
            );
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK')")
    @PutMapping("/update")
    public ResponseEntity<?> update(HttpServletRequest request, @RequestBody String requestBody,@RequestParam String id) {
        if(Utils.notBlankString(id)){
            CustomerRequest customerRequest = JsonUtils.fromJson(requestBody, CustomerRequest.class);

            CustomerModel customerModel = customerService.getCustomerById(id);
            if(customerModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "Customer '"+id+"' not found",
                        "/api/customers/update?id=" + id
                );
            }
            CustomerDto customerDto = CustomerMapper.toDto(customerModel);
            CustomerMapper.updateFromRequest(customerDto, customerRequest);

            customerService.saveCustomer(CustomerMapper.toModel(customerDto));
            auditLogService.log(request, AuditAction.UPDATE_CUSTOMER, customerDto.getId(), JsonUtils.toJson(customerDto));
            return ApiResponse.success(HttpStatus.OK, "Customer updated successfully","/api/customers/update?id=" +id, customerDto);
        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank ID",
                    "/api/customers/update?id=" + id
            );
        }

    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK', 'EMPLOYEE')")
    @GetMapping
    public ResponseEntity<?> getAllCustomers(HttpServletRequest request) {
        List<CustomerModel> customers = customerService.getAllCustomers();
        System.out.println(customers.size());
        if(customers.isEmpty()){
            System.err.println("No customers found");
        }
        List<CustomerDto> customerDtoList = CustomerMapper.toDto(customers);
        List<String> ids = customerDtoList.stream()
                .map(CustomerDto::getId)
                .collect(Collectors.toList());
        auditLogService.log(request, AuditAction.GET_CUSTOMERS, String.valueOf(customerDtoList.size()), JsonUtils.toJson(ids));
        return ApiResponse.success(HttpStatus.OK,"Got all Customers.", "/api/customers", customerDtoList);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> searchCustomers(HttpServletRequest request, @RequestParam String id){
        if(customerService.deleteCustomerById(id)){
            auditLogService.log(request, AuditAction.DELETE_CUSTOMER, id, null);
            return ApiResponse.success(HttpStatus.OK,"Deleted user '"+id+"'", "/api/customers/delete?id=" + id, null);
        }
        return ApiResponse.error(HttpStatus.NOT_FOUND,"Could not delete user user '"+id+"'", "/api/customers/delete?id=" + id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK', 'EMPLOYEE')")
    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(HttpServletRequest request, CustomerSearchRequest search) {

        if (search.getId() != null && !search.getId().isBlank()) {
            var model = customerService.getCustomerById(search.getId());
            if (model != null) {
                var dto = CustomerMapper.toDto(model);
                auditLogService.log(request, AuditAction.GET_CUSTOMERS, "1", "[" + dto.getId() + "]");
                return ApiResponse.success(HttpStatus.OK, "Customer Found.", "/api/customers/search?id=" + dto.getId(), dto);
            }
            return ApiResponse.error(HttpStatus.NOT_FOUND, "No customer with ID '" + search.getId() + "' found.", "/api/customers/search");
        }

        return searchByOtherField(request, search);
    }

    private ResponseEntity<?> searchByOtherField(HttpServletRequest request, CustomerSearchRequest search) {
        if (search.getFirst() != null && !search.getFirst().isBlank()) {
            return respondList("first name", search.getFirst(), customerService.searchByFirstName(search.getFirst()), request);
        }
        if (search.getLast() != null && !search.getLast().isBlank()) {
            return respondList("last name", search.getLast(), customerService.searchByLastName(search.getLast()), request);
        }
        if (search.getPhone() != null && !search.getPhone().isBlank()) {
            return respondList("phone", search.getPhone(), customerService.searchByPhone(search.getPhone()), request);
        }
        if (search.getEmail() != null && !search.getEmail().isBlank()) {
            return respondList("email", search.getEmail(), customerService.searchByEmail(search.getEmail()), request);
        }

        return ApiResponse.error(HttpStatus.BAD_REQUEST, "Not a valid search", "/api/customers/search");
    }

    private ResponseEntity<?> respondList(String fieldLabel, String query, List<CustomerModel> results, HttpServletRequest request) {
        if (results == null || results.isEmpty()) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, "No customer with " + fieldLabel + " like '" + query + "' found.", "/api/customers/search?" + fieldLabel + "=" + query);
        }

        var dtos = CustomerMapper.toDto(results);
        var ids = dtos.stream().map(CustomerDto::getId).toList();
        auditLogService.log(request, AuditAction.GET_CUSTOMERS, String.valueOf(dtos.size()), JsonUtils.toJson(ids));

        return ApiResponse.success(HttpStatus.OK, "Found " + dtos.size() + " customer(s) with " + fieldLabel + " like '" + query + "'", "/api/customers/search?" + fieldLabel + "=" + query, dtos);
    }



}
