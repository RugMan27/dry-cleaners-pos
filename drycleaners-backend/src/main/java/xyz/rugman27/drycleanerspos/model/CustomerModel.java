package xyz.rugman27.drycleanerspos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;


@Entity
@Data
@Table(name = "customers")
public class CustomerModel {

    @Id
    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private  String id;

    @Column(nullable = false)
    private String lastName;

    private String firstName;


    private String phone;

    private String email;


    @Column(name = "created_at", nullable = false)
    @Setter(AccessLevel.NONE)
    private Long createdAt = System.currentTimeMillis();


    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    public CustomerModel(String id, Long createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.extraData = "{}"; // or default JSON
    }

    public CustomerModel() {

    }

    public CustomerModel(String id, String lastName) {
        this.id = id;
        this.lastName = lastName;
    }
}
