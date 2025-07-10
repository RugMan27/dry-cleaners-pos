package xyz.rugman27.drycleanerspos.dto;

import lombok.Data;
import xyz.rugman27.drycleanerspos.data.ServiceType;

import java.util.Objects;

@Data
public class InvoiceItemDto {
    private String description;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private ServiceType serviceType;
    private PriceType priceType;

    public void calculate() {

        if(this.priceType == null){
            this.priceType = PriceType.CALCULATED;
        }

        if(this.priceType == PriceType.CALCULATED) {
            this.totalPrice = unitPrice * quantity;
        }
    }

    public enum PriceType {
        CALCULATED,
        MANUAL,
        AWAITING_MANAGER_APPROVAL,
        MANAGER_APPROVED
    }


    public InvoiceItemDto(String description, int quantity, double unitPrice, ServiceType serviceType) {
        this.description = Objects.requireNonNull(description);
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
        this.serviceType = serviceType;
        this.priceType = InvoiceItemDto.PriceType.CALCULATED;
    }

    public void setTotalPrice(double totalPrice, PriceType priceType) {
        this.priceType = priceType;
        if(priceType == PriceType.CALCULATED) {
            this.totalPrice = unitPrice * quantity;
            return;
        }
        if(priceType == PriceType.MANUAL) {
            this.totalPrice = totalPrice;
            return;
        }
        if(priceType == PriceType.AWAITING_MANAGER_APPROVAL) {
            this.totalPrice = totalPrice;
        }
        if(priceType == PriceType.MANAGER_APPROVED) {
            this.totalPrice = totalPrice;
        }
    }



    public InvoiceItemDto(){

    }
}