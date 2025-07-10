package xyz.rugman27.drycleanerspos.mapper;

import xyz.rugman27.drycleanerspos.dto.EventRequest;
import xyz.rugman27.drycleanerspos.dto.EventDto;
import xyz.rugman27.drycleanerspos.model.EventModel;
import xyz.rugman27.drycleanerspos.utilites.JsonUtils;
import xyz.rugman27.drycleanerspos.utilites.MergeUtils;

import java.util.ArrayList;
import java.util.List;

public class EventMapper {


    public static EventDto toDto(EventModel model) {
        if (model == null) return null;

        EventDto dto = new EventDto();
        dto.setId(model.getId());
        dto.setStart(model.getStart());
        dto.setEnd(model.getEnd());
        dto.setEmployeeId(model.getEmployeeId());
        dto.setCalendarId(model.getCalendarId());

        try {
            EventDto metadataDto = JsonUtils.fromJson(model.getMetadata(), EventDto.class);

            // Copy metadata fields into dto
            dto.setTitle(metadataDto.getTitle());
            dto.setDescription(metadataDto.getDescription());
            dto.setLocation(metadataDto.getLocation());
            dto.setPeople(metadataDto.getPeople());
            dto.setOptions(metadataDto.getOptions());
            dto.setCustomContent(metadataDto.getCustomContent());
            dto.setExtra(metadataDto.getExtra());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    public static EventModel toModel(EventDto dto) {
        if (dto == null) return null;

        EventModel model = new EventModel();
        model.setId(dto.getId());
        model.setStart(dto.getStart());
        model.setEnd(dto.getEnd());
        model.setEmployeeId(dto.getEmployeeId());
        model.setCalendarId(dto.getCalendarId());

        try {
            // Serialize full DTO as metadata
            String metadataJson = JsonUtils.toJson(dto);
            model.setMetadata(metadataJson);
        } catch (Exception e) {
            e.printStackTrace();
            model.setMetadata("{}");
        }

        return model;
    }
    public static void updateFromRequest(EventDto dto, EventRequest req) {
        if (dto == null) return;
        if (req.getStart() != null) dto.setStart(req.getStart());
        if (req.getEnd() != null) dto.setEnd(req.getEnd());
        if (req.getEmployeeId() != null) dto.setEmployeeId(req.getEmployeeId());
        if (req.getCalendarId() != null) dto.setCalendarId(req.getCalendarId());
        if (req.getTitle() != null) dto.setTitle(req.getTitle());
        if (req.getDescription() != null) dto.setDescription(req.getDescription());
        if (req.getLocation() != null) dto.setLocation(req.getLocation());
        if (req.getPeople() != null) dto.setPeople(req.getPeople());

        if (req.getExtra() != null )  MergeUtils.mergeNonNullFields(req.getExtra(), dto.getExtra());
        if (req.getOptions() != null) MergeUtils.mergeNonNullFields(req.getOptions(), dto.getOptions());
        if (req.getCustomContent() != null) MergeUtils.mergeNonNullFields(req.getCustomContent(), dto.getCustomContent());

    }

    public static EventDto fromRequest(EventRequest model) {
        EventDto dto = new EventDto();
        updateFromRequest(dto, model);
        return dto;
    }

    public static List<EventDto> toDto(List<EventModel> models) {
        List<EventDto> dtos = new ArrayList<>();
        for (EventModel model : models) {
            dtos.add(toDto(model));
        }
        return dtos;
    }
}
