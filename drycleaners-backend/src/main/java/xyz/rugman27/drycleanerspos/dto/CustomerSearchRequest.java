package xyz.rugman27.drycleanerspos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSearchRequest {
    private String id;
    private String first;
    private String last;
    private String phone;
    private String email;
}
