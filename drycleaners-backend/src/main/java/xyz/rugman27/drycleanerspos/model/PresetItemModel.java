package xyz.rugman27.drycleanerspos.model;

import jakarta.persistence.*;
import lombok.Data;
import xyz.rugman27.drycleanerspos.data.ServiceType;

@Entity
@Table(name = "preset_items")
@Data
public class PresetItemModel {
    @Id
    @Column(length = 50)
    private String code;

    private String name; // Human-readable: "Dress Shirt", "Blouse", etc.

    private double defaultPrice;

    private boolean managerPrice;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private boolean enabled;

}
