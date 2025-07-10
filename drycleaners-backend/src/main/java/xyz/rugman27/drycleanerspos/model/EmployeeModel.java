package xyz.rugman27.drycleanerspos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import xyz.rugman27.drycleanerspos.utilites.PasswordUtils;

import java.util.UUID;

@Entity
@Data
@Table(name = "employees")
public class EmployeeModel {


    public EmployeeModel(String id, Long createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public enum EmployeeType {
        EMPLOYEE,
        CLERK,
        MANAGER,
        ADMIN,
        OWNER
    }

    @Id
    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private String id;

    @Column(nullable = false)
    private String lastName;

    private String firstName;

    private String phone;

    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeType employeeType = EmployeeType.EMPLOYEE;

    @Column(name = "created_at", nullable = false)
    @Setter(AccessLevel.NONE)
    private Long createdAt = System.currentTimeMillis();

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    @JsonIgnore
    @Column(name = "profile_pic",  columnDefinition = "TEXT")
    private String profilePic;


    private UUID photoUuid;

    public EmployeeModel(String id, String firstName, String lastName, String phone, String email, String username, String passwordHash, long createdAt, String extraData, boolean enabled) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.extraData = extraData;
        this.enabled = enabled;
    }

    public EmployeeModel(String id, String lastName, String username, String password) {
        this.id = id;
        this.lastName = lastName;
        this.username = username;
        this.passwordHash = PasswordUtils.hashPassword(password);
        this.extraData = "{}"; // default empty JSON
    }

    public EmployeeModel() {
        // No-arg constructor for JPA
    }
}
