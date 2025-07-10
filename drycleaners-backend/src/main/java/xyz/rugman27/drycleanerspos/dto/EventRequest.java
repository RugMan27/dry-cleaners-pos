package xyz.rugman27.drycleanerspos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class EventRequest {
    private Long id;
    private String start;
    private String end;
    private String employeeId;
    private String calendarId;
    private String title;
    private String description;
    private String location;
    private List<String> people;

    @JsonAlias("_options")
    private EventDto.Options options;

    @JsonAlias("_custom_content")
    @JsonProperty("_customContent")
    private EventDto.CustomContent customContent;

    private EventDto.ExtraData extra;

}
