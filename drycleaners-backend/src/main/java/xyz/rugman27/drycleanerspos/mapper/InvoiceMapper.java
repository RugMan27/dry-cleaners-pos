package xyz.rugman27.drycleanerspos.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import xyz.rugman27.drycleanerspos.data.Tag;
import xyz.rugman27.drycleanerspos.dto.InvoiceDto;
import xyz.rugman27.drycleanerspos.dto.InvoiceRequest;
import xyz.rugman27.drycleanerspos.dto.InvoiceItemDto;
import xyz.rugman27.drycleanerspos.model.InvoiceModel;
import xyz.rugman27.drycleanerspos.utilites.JsonUtils;
import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;

public class InvoiceMapper {


    public static InvoiceModel toModel(InvoiceDto dto) {
        InvoiceModel model = new InvoiceModel(dto.getId(),  dto.getDateCreated());
        model.setServiceType(dto.getServiceType());
        model.setEmployeeId(dto.getEmployeeId());
        model.setCustomerId(dto.getCustomerId());
        model.setCustomerPhone(PhoneNumberUtil.normalizeForStorage(dto.getCustomerPhone()));
        model.setCustomerName(dto.getCustomerName());
        model.setItemsJson(JsonUtils.toJson(dto.getItems()));
        model.setPaymentInfoJson(JsonUtils.toJson(dto.getPaymentInfo()));
        model.setNotes(dto.getNotes());
        model.setInvoiceStatus(dto.getStatus());
        model.setPaymentStatus(dto.getPaymentStatus());
        model.setTagsJson(JsonUtils.toJson(dto.getTags()));
        model.setManagerAttention(dto.isManagerAttention());
        model.setDatePaid(dto.getPaymentInfo().getPaymentDate());

        return model;
    }

    public static InvoiceDto toDto(InvoiceModel model) {
        InvoiceDto dto = new InvoiceDto(model.getId(), model.getServiceType(), model.getDateCreated());
        dto.setEmployeeId(model.getEmployeeId());
        dto.setCustomerId(model.getCustomerId());
        dto.setCustomerPhone(model.getCustomerPhone());
        dto.setCustomerName(model.getCustomerName());

        dto.setManagerAttention(model.isManagerAttention());

        dto.setNotes(model.getNotes());
        dto.setStatus(model.getInvoiceStatus());
        dto.setPaymentStatus(model.getPaymentStatus());


        dto.setItems(JsonUtils.fromJson(model.getItemsJson(),
                new TypeReference<List<InvoiceItemDto>>() {}));

        dto.setPaymentInfo(JsonUtils.fromJson(model.getPaymentInfoJson(), InvoiceDto.PaymentInfo.class));

        dto.setTags(JsonUtils.fromJson(model.getTagsJson(),
                new TypeReference<List<Tag>>() {}));


        return dto;
    }
    public static void updateFromRequest(InvoiceDto dto, InvoiceRequest req) {
        if (req.getEmployeeId() != null) dto.setEmployeeId(req.getEmployeeId());
        if (req.getCustomerId() != null) dto.setCustomerId(req.getCustomerId());
        if (req.getCustomerPhone() != null) dto.setCustomerPhone(req.getCustomerPhone());
        if (req.getCustomerName() != null) dto.setCustomerName(req.getCustomerName());
        if (req.getItems() != null && !req.getItems().isEmpty()) dto.setItems(req.getItems());
        if (req.getPaymentInfo() != null) dto.setPaymentInfo(req.getPaymentInfo());
        if (req.getNotes() != null) dto.setNotes(req.getNotes());
        if (req.getStatus() != null) dto.setStatus(req.getStatus());
        if (req.getTags() != null && !req.getTags().isEmpty()) dto.setTags(req.getTags());
    }

    public static List<InvoiceDto> toDto(List<InvoiceModel> all) {
        List<InvoiceDto> dtos = new ArrayList<>();
        for (InvoiceModel model : all) {
            dtos.add(toDto(model));
        }
        return dtos;
    }
}
