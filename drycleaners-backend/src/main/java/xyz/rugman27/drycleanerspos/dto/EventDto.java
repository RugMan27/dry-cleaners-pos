package xyz.rugman27.drycleanerspos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String start;
    private String end;
    private String employeeId;
    private String calendarId;
    private String title;
    private String description;
    private String location;
    private List<String> people;

    @JsonProperty("_options")
    private Options options;

    @JsonProperty("_customContent")
    private CustomContent customContent;

    private ExtraData extra;


    public ExtraData getExtra() {
        if (extra == null){
            extra = new ExtraData(); // ← fix here
        }
        return extra;
    }

    public Options getOptions() {
        if (options == null) {
            options = new Options(); // ← fix here
        }
        return options;
    }

    public CustomContent getCustomContent() {
        if (customContent == null){
            customContent = new CustomContent(); // ← fix here
        }
        return customContent;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Options {
        @JsonProperty("disableDND")
        private Boolean disableDND;
        @JsonProperty("disableResize")
        private Boolean disableResize;
        @JsonProperty("additionalClasses")
        private List<String> additionalClasses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomContent {
        private String timeGrid;
        private String dateGrid;
        private String monthGrid;
        private String monthAgenda;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraData {
        private String color;
        private String employeeId;
    }
}
