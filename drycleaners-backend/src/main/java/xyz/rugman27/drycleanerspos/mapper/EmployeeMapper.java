package xyz.rugman27.drycleanerspos.mapper;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.rugman27.drycleanerspos.dto.EmployeeDto;
import xyz.rugman27.drycleanerspos.dto.EmployeeRequest;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;
import xyz.rugman27.drycleanerspos.service.EmployeeService;
import xyz.rugman27.drycleanerspos.utilites.JsonUtils;
import xyz.rugman27.drycleanerspos.utilites.MergeUtils;
import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;

import java.util.*;

@Component
public class EmployeeMapper {

    @Autowired
    private EmployeeService employeeService;

    private static EmployeeService staticEmployeeService;

    @PostConstruct
    public void init() {
        staticEmployeeService = this.employeeService;
    }

    public static EmployeeDto toDto(EmployeeModel model) {
        if (model == null) return null;

        if(model.getPhotoUuid() == null) {
            model.setPhotoUuid(UUID.randomUUID());
            staticEmployeeService.saveEmployee(model);
        }



        EmployeeDto dto = new EmployeeDto(model.getId(), model.getCreatedAt());
        dto.setFirstName(model.getFirstName());
        dto.setLastName(model.getLastName());
        dto.setPhone(model.getPhone());
        dto.setEmail(model.getEmail());
        dto.setUsername(model.getUsername());
        dto.setPasswordHash(model.getPasswordHash());
        dto.setEnabled(model.isEnabled());
        dto.setEmployeeType(model.getEmployeeType());

        dto.setProfilePicBase64(model.getProfilePic());



        dto.setPhotoUuid(model.getPhotoUuid());

        if (model.getExtraData() != null) {
            dto.setExtraData(JsonUtils.fromJson(model.getExtraData(), EmployeeDto.ExtraJsonData.class));
        } else {
            dto.setExtraData(new EmployeeDto.ExtraJsonData());
        }

        dto.setOriginalModel(model);
        return dto;
    }
    public static List<EmployeeDto> toDto(List<EmployeeModel> employeeModelList) {
        List<EmployeeDto> dtoList = new ArrayList<>();
        for (EmployeeModel employeeModel : employeeModelList) {
            dtoList.add(toDto(employeeModel));
        }
        return dtoList;
    }

    public static EmployeeModel toModel(EmployeeDto dto) {
        if (dto == null) return null;

        EmployeeModel model = dto.getOriginalModel();
        if (model == null) {
            model = new EmployeeModel(dto.getId(), dto.getCreatedAt());
        }

        if(model.getPhotoUuid() == null) {
            model.setPhotoUuid(UUID.randomUUID());
        }

        model.setFirstName(dto.getFirstName());
        model.setLastName(dto.getLastName());
        model.setPhone(PhoneNumberUtil.normalizeForStorage(dto.getPhone()));
        model.setEmail(dto.getEmail());
        model.setUsername(dto.getUsername());
        model.setPasswordHash(dto.getPasswordHash());
        model.setEnabled(dto.isEnabled());
        model.setEmployeeType(dto.getEmployeeType());

        model.setProfilePic(dto.getProfilePicBase64());

        model.setPhotoUuid(dto.getPhotoUuid());

        if (dto.getExtraData() != null) {
            model.setExtraData(JsonUtils.toJson(dto.getExtraData()));
        } else {
            model.setExtraData(null);
        }

        return model;
    }
    public static void updateFromRequest(EmployeeDto employeeDto, EmployeeRequest employeeRequest) {
        System.out.println(JsonUtils.toJson(employeeRequest));
        if (employeeRequest.getFirstName() != null) {
            employeeDto.setFirstName(employeeRequest.getFirstName());
        }
        if (employeeRequest.getLastName() != null) {
            employeeDto.setLastName(employeeRequest.getLastName());
        }
        if (employeeRequest.getPhone() != null) {
            employeeDto.setPhone(PhoneNumberUtil.formatReadable(employeeRequest.getPhone()));
        }
        if (employeeRequest.getEmail() != null) {
            employeeDto.setEmail(employeeRequest.getEmail());
        }
        if (employeeRequest.getUsername() != null) {
            employeeDto.setUsername(employeeRequest.getUsername());
        }
        if (employeeRequest.getEnabled() != null) {
            employeeDto.setEnabled(employeeRequest.getEnabled());
        }
        if(employeeRequest.getEmployeeType() != null) {
            employeeDto.setEmployeeType(employeeRequest.getEmployeeType());
        }
        if (employeeRequest.getExtraData() != null) {
            MergeUtils.mergeNonNullFields(employeeRequest.getExtraData(), employeeDto.getExtraData());
        }
        if(employeeRequest.getImageBase64() != null) {
            employeeDto.setProfilePicBase64(employeeRequest.getImageBase64());
        }
        System.out.println(employeeDto.getExtraData().getActive());
    }
}
