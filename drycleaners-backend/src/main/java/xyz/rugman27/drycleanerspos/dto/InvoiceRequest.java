package xyz.rugman27.drycleanerspos.dto;

import lombok.Data;
import lombok.Getter;
import xyz.rugman27.drycleanerspos.data.ServiceType;
import xyz.rugman27.drycleanerspos.data.Tag;

import java.util.ArrayList;
import java.util.List;
@Data
public class InvoiceRequest {
    private ServiceType serviceType;
    private String employeeId;
    private String customerId;
    private String customerPhone;
    private String customerName;
    private List<InvoiceItemDto> items;
    private InvoiceDto.PaymentInfo paymentInfo;
    private String notes;
    private InvoiceDto.InvoiceStatus status;
    private List<Tag> tags;

}
