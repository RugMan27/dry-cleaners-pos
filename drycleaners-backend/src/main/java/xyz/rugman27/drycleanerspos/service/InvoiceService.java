package xyz.rugman27.drycleanerspos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xyz.rugman27.drycleanerspos.dto.InvoiceDto;
import xyz.rugman27.drycleanerspos.dto.InvoiceRequest;
import xyz.rugman27.drycleanerspos.dto.InvoiceSearchRequest;
import xyz.rugman27.drycleanerspos.mapper.InvoiceMapper;
import xyz.rugman27.drycleanerspos.model.InvoiceModel;
import xyz.rugman27.drycleanerspos.repository.InvoiceRepository;
import xyz.rugman27.drycleanerspos.utilites.Utils;
import xyz.rugman27.drycleanerspos.dto.InvoiceDto.InvoiceStatus;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public InvoiceModel save(InvoiceModel cleaningInvoice) {
        if(cleaningInvoice.getPaymentStatus() == null){
            cleaningInvoice.setPaymentStatus(InvoiceDto.PaymentStatus.UNPAID);
        }
        return invoiceRepository.save(cleaningInvoice);


    }
    public InvoiceModel getById(String id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    public InvoiceDto createNewCleaningInvoice(InvoiceRequest invoiceRequest) {
        InvoiceDto invoiceDto = new InvoiceDto(generateInvoiceId(invoiceRequest.getServiceType().getCode()), invoiceRequest.getServiceType());
        InvoiceMapper.updateFromRequest(invoiceDto, invoiceRequest);
        return invoiceDto;

    }

    private String generateInvoiceId(char typePrefix) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear()).substring(2); // "25"
        char monthCode = (char) ('A' + now.getMonthValue() - 1);  // A–L

        String prefix = String.format("I%c%s%c", typePrefix, year, monthCode);

        InvoiceModel lastInvoice = invoiceRepository.findTopByIdStartingWithOrderByIdDesc(prefix);
        int nextIndex = getNextIndex(lastInvoice, prefix);

        int block = nextIndex / 100;
        int number = nextIndex % 100;
        char blockAlpha = (char) ('A' + block);

        return String.format("%s%02d%c", prefix, number, blockAlpha); // IL25F00A
    }

    private static int getNextIndex(InvoiceModel lastInvoice, String prefix) {
        String lastId = lastInvoice != null ? lastInvoice.getId() : null;

        int nextIndex = 0;
        if (lastId != null && lastId.length() >= prefix.length() + 3) {
            String numericPart = lastId.substring(prefix.length(), prefix.length() + 2); // "00"–"99"
            char blockChar = lastId.charAt(prefix.length() + 2); // 'A'–'Z'

            try {
                int block = blockChar - 'A';
                int number = Integer.parseInt(numericPart);
                nextIndex = block * 100 + number + 1;
            } catch (Exception ignored) {}
        }
        return nextIndex;
    }


    public List<InvoiceModel> getAll() {
        return invoiceRepository.findAll();
    }

    public boolean deleteByID(String id) {
        if(!invoiceRepository.existsById(id)){ return false; }
        invoiceRepository.deleteById(id);
        return true;
    }



    public List<InvoiceModel> searchInvoices(InvoiceSearchRequest search) {

        LocalDate paymentFromDate = search.getPaymentFromDate();
        LocalDate paymentToDate = search.getPaymentToDate();

        System.out.println("DEBUG: LocalDate from search.getPaymentFromDate(): " + paymentFromDate); // Check this!
        System.out.println("DEBUG: LocalDate from search.getPaymentToDate(): " + paymentToDate);   // Check this!


        Specification<InvoiceModel> spec = (root, query, cb) -> cb.conjunction();


        if (Utils.notBlankString(search.getId())) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), search.getId()));
        }

        if (Utils.notBlankString(search.getCustomerId())) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("customerId"), search.getCustomerId()));
        }

        if (Utils.notBlankString(search.getCustomerName())) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("customerName")), "%" + search.getCustomerName().toLowerCase() + "%"));
        }

        if (search.getCreatedFromDate() != null) {
            Long fromEpoch = Utils.toEpochMillis(search.getCreatedFromDate());
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dateCreated"), fromEpoch));
        }

        if (search.getCreatedToDate() != null) {
            Long toEpoch = Utils.toEpochMillis(search.getCreatedToDate());
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dateCreated"), toEpoch));
        }

        if (search.getPaymentFromDate() != null) {
            Long fromEpoch = Utils.toEpochMillisStartOfDay(search.getPaymentFromDate());
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("datePaid"), fromEpoch));
        }

        if (search.getPaymentToDate() != null) {
            Long toEpoch = Utils.toEpochMillisEndOfDay(search.getPaymentToDate());
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("datePaid"), toEpoch));
        }

        if (search.getInvoiceStatus() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("invoiceStatus"), InvoiceStatus.valueOf(search.getInvoiceStatus().name())));
        } else if (Boolean.TRUE.equals(search.getExcludePickedUp())) {
            System.out.println("DEBUG: Exclude PICKED_UP (including nulls)");

            spec = spec.and((root, query, cb) -> cb.or(
                    cb.notEqual(root.get("invoiceStatus"), InvoiceDto.InvoiceStatus.PICKED_UP),
                    cb.isNull(root.get("invoiceStatus"))
            ));
        }



        if (search.getPaymentStatus() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("paymentStatus"), search.getPaymentStatus()));
        } else if (Boolean.TRUE.equals(search.getExcludePaid())) {

            spec = spec.and((root, query, cb) ->
                    cb.notEqual(root.get("paymentStatus"), InvoiceDto.PaymentStatus.PAID));
        }


        if (search.getServiceType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("serviceType"), search.getServiceType()));
        }

        if (Utils.notBlankString(search.getTagContains())) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("tagsJson")), "%" + search.getTagContains().toLowerCase() + "%"));
        }

        return invoiceRepository.findAll(spec);
    }


}
