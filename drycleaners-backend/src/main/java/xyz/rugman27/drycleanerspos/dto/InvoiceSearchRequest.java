package xyz.rugman27.drycleanerspos.dto;

import lombok.Data;
import xyz.rugman27.drycleanerspos.data.ServiceType;

import java.time.LocalDate;

@Data
public class InvoiceSearchRequest {
    private String id;
    private String customerId;
    private String employeeId;
    private String customerPhone;
    private String customerName;
    private InvoiceDto.PaymentStatus paymentStatus;
    private InvoiceDto.InvoiceStatus invoiceStatus;
    private ServiceType serviceType;

    private Boolean excludePaid;
    private Boolean excludePickedUp;

    private LocalDate createdFromDate;
    private LocalDate createdToDate;

    private LocalDate paymentFromDate;
    private LocalDate paymentToDate;



    private String tagContains; // Optional keyword to search inside tagsJson
}
