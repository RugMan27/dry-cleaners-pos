package xyz.rugman27.drycleanerspos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;

@Getter
public class EmployeeRequest {

    private String id;
    private String lastName;
    private String firstName;
    private String phone;
    private String email;
    private String username;
    private String password;
    private Boolean enabled;
    private EmployeeModel.EmployeeType employeeType;
    private EmployeeDto.ExtraJsonData extraData;
    @JsonAlias("image_base64")
    private String imageBase64;


}
