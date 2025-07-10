package xyz.rugman27.drycleanerspos.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import xyz.rugman27.drycleanerspos.data.ServiceType;
import xyz.rugman27.drycleanerspos.dto.InvoiceDto;

import java.time.Instant;

@Entity
@Table(name = "cleaning_invoices")
@Data
public class InvoiceModel {

    @Id
    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private long dateCreated;  // epoch millis

    private String employeeId;

    private String customerId;

    private String customerPhone;

    private String customerName;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;  // JSON string of List<CleaningItemDto>

    @Column(columnDefinition = "TEXT")
    private String paymentInfoJson;  // JSON string of PaymentInfo

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private InvoiceDto.InvoiceStatus invoiceStatus;

    @Enumerated(EnumType.STRING)
    private InvoiceDto.PaymentStatus paymentStatus;

    @Column(columnDefinition = "TEXT")
    private String tagsJson;  // JSON string of List<Tag>

    private boolean managerAttention;

    private Long datePaid;


    // Optional: Constructor for new invoices
    public InvoiceModel(String id) {
        this.id = id;
        this.dateCreated = Instant.now().toEpochMilli();
    }
    public InvoiceModel(String id, Long dateCreated) {
        this.id = id;
        this.dateCreated = dateCreated;
    }

    public InvoiceModel() {
        this.dateCreated = Instant.now().toEpochMilli();
    }
}
