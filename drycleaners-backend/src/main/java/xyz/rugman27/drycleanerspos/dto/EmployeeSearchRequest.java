package xyz.rugman27.drycleanerspos.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class EmployeeSearchRequest {
    private String id;
    private String user;
    private String last;
}
