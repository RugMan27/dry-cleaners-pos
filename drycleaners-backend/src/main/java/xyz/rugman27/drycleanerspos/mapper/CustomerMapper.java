package xyz.rugman27.drycleanerspos.mapper;


import xyz.rugman27.drycleanerspos.dto.CustomerDto;
import xyz.rugman27.drycleanerspos.model.CustomerModel;
import xyz.rugman27.drycleanerspos.dto.CustomerRequest;
import xyz.rugman27.drycleanerspos.utilites.JsonUtils;
import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;
import xyz.rugman27.drycleanerspos.utilites.MergeUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomerMapper {

    // Model → DTO
    public static CustomerDto toDto(CustomerModel model) {
        if (model == null) return null;

        CustomerDto dto = new CustomerDto(model.getId(), model.getCreatedAt());
        dto.setFirstName(model.getFirstName());
        dto.setLastName(model.getLastName());
        dto.setPhone(PhoneNumberUtil.formatReadable(model.getPhone()));
        dto.setEmail(model.getEmail());

        // Parse JSON for extraData or create default
        CustomerDto.ExtraCustomerData extraData = model.getExtraData() != null
                ? JsonUtils.fromJson(model.getExtraData(), CustomerDto.ExtraCustomerData.class)
                : new CustomerDto.ExtraCustomerData();
        dto.setExtraData(extraData);

        // Store original model reference for updates if needed
        dto.setOriginalModel(model);

        return dto;
    }

    // DTO → Model
    public static CustomerModel toModel(CustomerDto dto) {
        if (dto == null) return null;

        CustomerModel model = dto.getOriginalModel();
        if (model == null) {
            model = new CustomerModel(dto.getId(), dto.getCreatedAt());
        }

        model.setFirstName(dto.getFirstName());
        model.setLastName(dto.getLastName());
        model.setPhone(PhoneNumberUtil.normalizeForStorage(dto.getPhone()));
        model.setEmail(dto.getEmail());

        if (dto.getExtraData() != null) {
            model.setExtraData(JsonUtils.toJson(dto.getExtraData()));
        } else {
            model.setExtraData("{}");
        }

        return model;
    }

    public static List<CustomerDto> toDto(List<CustomerModel> customerModelList){
        List<CustomerDto> dtoList = new ArrayList<>();
        for (CustomerModel customerModel : customerModelList) {
            CustomerDto dto = toDto(customerModel);
            dtoList.add(dto);
        }
        return  dtoList;
    }

    // Update DTO from request (partial updates)
    public static void updateFromRequest(CustomerDto dto, CustomerRequest request) {
        if (dto == null || request == null) return;

        if (request.getFirstName() != null) {
            dto.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            dto.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            dto.setPhone(PhoneNumberUtil.formatReadable(request.getPhone()));
        }
        if (request.getEmail() != null) {
            dto.setEmail(request.getEmail());
        }
        if (request.getExtraData() != null) {
            if (dto.getExtraData() == null) {
                dto.setExtraData(new CustomerDto.ExtraCustomerData());
            }
            MergeUtils.mergeNonNullFields(request.getExtraData(), dto.getExtraData());
        }
    }
}
