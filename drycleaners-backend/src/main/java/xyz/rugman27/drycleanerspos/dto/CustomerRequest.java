package xyz.rugman27.drycleanerspos.dto;

import lombok.Getter;

@Getter
public class CustomerRequest {

    private String id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private Long createdAt;
    private CustomerDto.ExtraCustomerData extraData;

}
