package xyz.rugman27.drycleanerspos.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Tag {

    private int count;            // number of items with this tag
    private String tagNumber;     // 3-digit tag number as a string (e.g., "023")
    private TagDay tagDay;        // Day this tag belongs to (includes color meaning)

    public Tag(int count, String tagNumber, TagDay tagDay) {
        this.count = count;
        this.tagNumber = tagNumber;
        this.tagDay = tagDay;
    }

    // Getters/setters...
}
