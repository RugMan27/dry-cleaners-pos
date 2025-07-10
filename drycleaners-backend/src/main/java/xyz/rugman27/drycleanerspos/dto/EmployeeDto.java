package xyz.rugman27.drycleanerspos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import xyz.rugman27.drycleanerspos.model.CustomerModel;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;
import xyz.rugman27.drycleanerspos.utilites.MergeUtils;
import xyz.rugman27.drycleanerspos.utilites.PasswordUtils;
import xyz.rugman27.drycleanerspos.utilites.PhoneNumberUtil;

import java.util.UUID;

@Data
public class EmployeeDto {

    @Setter(AccessLevel.NONE)
    private String id;
    private String lastName;
    private String firstName;
    private String phone;
    private String email;
    private String username;
    private String passwordHash;
    private boolean enabled;
    private EmployeeModel.EmployeeType employeeType;
    @Setter(AccessLevel.NONE)
    private Long createdAt;
    private EmployeeDto.ExtraJsonData extraData;
    @JsonIgnore
    private String profilePicBase64;

    private String photoUrl;

    @JsonIgnore
    private transient EmployeeModel originalModel;
    @JsonIgnore
    private UUID photoUuid;


    public void setPhotoUuid(UUID photoUuid) {
        this.photoUuid = photoUuid;
        if(profilePicBase64 != null) {
            this.photoUrl = "http://localhost:8080/api/employees/photos/"+photoUuid.toString()+".jpg";
        }
    }


    public EmployeeDto(String id, Long createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }


    public void setNewPassword(String password) {
        this.passwordHash = PasswordUtils.hashPassword(password);
        originalModel.setPasswordHash(passwordHash);
    }




    @Data
    public static class ExtraJsonData {

        @JsonAlias({"active", "isActive"})
        private Boolean active;

        @JsonAlias({"last_login", "lastLogin"})
        private Long lastLogin;

        @JsonAlias({"login_count", "loginCount"})
        private int loginCount;

        @JsonAlias({"expire_time", "expireTime"})
        private Long expireTime;

        @JsonAlias({"notes", "note"})
        private String notes;

        private String color;
    }
}
