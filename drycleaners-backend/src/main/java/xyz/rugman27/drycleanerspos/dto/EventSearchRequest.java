package xyz.rugman27.drycleanerspos.dto;

import lombok.Data;

@Data
public class EventSearchRequest {
    private String id;
    private String from;
    private String to;
    private String employeeId;
    // add filters if needed...
}