package xyz.rugman27.drycleanerspos.dto;

import lombok.*;
import xyz.rugman27.drycleanerspos.config.TaxConfig;
import xyz.rugman27.drycleanerspos.data.ServiceType;
import xyz.rugman27.drycleanerspos.data.Tag;

import java.util.List;
import java.util.ArrayList;



@Data
public class InvoiceDto {

    @Setter(AccessLevel.NONE)
    private String id;
    @Setter(AccessLevel.NONE)
    private ServiceType serviceType;
    @Setter(AccessLevel.NONE)
    private long dateCreated;
    private String employeeId;
    private String customerId;
    private String customerPhone;
    private String customerName;
    private List<InvoiceItemDto> items = new ArrayList<>();
    private PaymentInfo paymentInfo;
    private String notes;
    private InvoiceStatus status;
    private List<Tag> tags = new ArrayList<>();
    private PaymentStatus paymentStatus;

    private boolean managerAttention;

    public void setItems(List<InvoiceItemDto> items) {
        this.items = items;

        validate();

    }

    public boolean checkManagerAttention() {
        this.managerAttention = false;
        for (InvoiceItemDto item : items) {
            if(item.getPriceType() == InvoiceItemDto.PriceType.AWAITING_MANAGER_APPROVAL){
                this.managerAttention = true;
                return true;
            }
        }
        return false;
    }
    public void validate() {

        // Items
        this.managerAttention = false;

        for (InvoiceItemDto item : items) {

            if(item.getPriceType() == null){
                item.setPriceType(InvoiceItemDto.PriceType.CALCULATED);
            }

            if(item.getPriceType() == InvoiceItemDto.PriceType.CALCULATED){
                item.calculate();
            }

            if(item.getPriceType() == InvoiceItemDto.PriceType.AWAITING_MANAGER_APPROVAL){
                this.managerAttention = true;
            }
        }

        // Total Price

        if(this.paymentStatus == null || this.paymentStatus == PaymentStatus.UNPAID){
            this.paymentStatus = PaymentStatus.UNPAID;

            if(this.paymentInfo == null){

            this.paymentInfo = new PaymentInfo();
            this.paymentInfo.calculate(this.items);


            }

        }

    }

    public InvoiceDto(String id, ServiceType serviceType, long dateCreated) {
        this.id = id;
        this.serviceType = serviceType;
        this.dateCreated = dateCreated;
    }

    public InvoiceDto(String id, ServiceType serviceType) {
        this.id = id;
        this.serviceType = serviceType;
        this.dateCreated = System.currentTimeMillis();
    }



    public enum PaymentStatus {
        PAID,
        UNPAID,
        ACCOUNT
    }



    public enum InvoiceStatus {
        DROPPED_OFF,
        TAGGED_IN,
        PUT_TOGETHER,
        PUT_AWAY,
        PICKED_UP,
        CANCELLED,
        REFUNDED
    }


    public enum PaymentMethod {
        CASH,
        CARD,
        ACCOUNT,
        OTHER,

    }

    // Nested class for payment details and financials
    @Data
    @NoArgsConstructor
    public static class PaymentInfo {
        private double subtotal;       // before tax & discount
        private double tax;            // tax amount
        private double discount;       // discount applied
        private double discountPercentage;       // discount applied
        private boolean taxAbsorbed;   // true if tax was absorbed by discount
        private double total;          // final amount customer pays
        private boolean paid;

        private String receiptNumber;
        private PaymentMethod paymentMethod;   // e.g. CASH, CARD, ACCOUNT
        private long paymentDate;       // epoch millis
        private boolean refunded;
        private String paymentNote;     // e.g. "Paid with gift card"



        private double roundUp(double value) {
            return Math.ceil(value * 100.0) / 100.0;
        }
        private double round(double value) {
            return Math.round(value * 100.0) / 100.0;
        }


        private void calculate(List<InvoiceItemDto> items) {
            this.subtotal = 0;
            for (InvoiceItemDto item : items) {
                item.calculate();
                subtotal += item.getTotalPrice();
            }

            this.discountPercentage = Math.min(100, Math.max(0, this.discountPercentage));
            this.discount = (discountPercentage / 100.0) * subtotal;
            this.tax = TaxConfig.getTaxPercentage() * subtotal;

            if (taxAbsorbed) {
                this.total = roundUp(subtotal - discount);
            } else {
                this.total = roundUp(subtotal + tax - discount);
            }
        }


    }


    // Constructors, getters/setters, etc., omitted for brevity
}

