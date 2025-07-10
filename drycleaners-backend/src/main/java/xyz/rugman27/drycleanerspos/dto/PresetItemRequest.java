package xyz.rugman27.drycleanerspos.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.Getter;
import xyz.rugman27.drycleanerspos.data.ServiceType;
@Data
public class PresetItemRequest {
    private String code;
    private String name;
    private Double defaultPrice;
    private Boolean managerPrice;
    private ServiceType serviceType;
}
