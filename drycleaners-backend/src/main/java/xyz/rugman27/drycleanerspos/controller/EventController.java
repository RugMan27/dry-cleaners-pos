package xyz.rugman27.drycleanerspos.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.rugman27.drycleanerspos.data.AuditAction;
import xyz.rugman27.drycleanerspos.dto.*;
import xyz.rugman27.drycleanerspos.mapper.EventMapper;
import xyz.rugman27.drycleanerspos.model.EventModel;
import xyz.rugman27.drycleanerspos.service.AuditLogService;
import xyz.rugman27.drycleanerspos.service.EventService;
import xyz.rugman27.drycleanerspos.utilites.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired private EventService eventService;
    @Autowired private AuditLogService auditLogService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @PostMapping("/create")
    public ResponseEntity<?> create(HttpServletRequest request, @RequestBody String requestBody) {
        EventRequest eventRequest = JsonUtils.fromJson(requestBody, EventRequest.class);
        System.out.println("eventRequest = " + eventRequest.getExtra().getColor());
        EventDto eventDto = EventMapper.fromRequest(eventRequest);
        System.out.println("dto = " + eventDto.getExtra().getColor());
        EventModel saved = eventService.save(EventMapper.toModel(eventDto));
        System.out.println("saved = " + JsonUtils.toJson(saved));
        EventDto finalDto = EventMapper.toDto(saved);
        System.out.println("finalDto = " + finalDto.getExtra().getColor());
        auditLogService.log(request, AuditAction.CREATE_EVENT, String.valueOf(finalDto.getId()), JsonUtils.toJson(finalDto));
        return ApiResponse.success(HttpStatus.CREATED, "Event created.", "/api/events/create", finalDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @PutMapping("/update")
    public ResponseEntity<?> update(HttpServletRequest request, @RequestBody String requestBody, @RequestParam String id) {
        EventModel existing = eventService.findById(id);
        if (existing == null) {
            return ApiResponse.error(HttpStatus.NOT_FOUND, "Event not found", "/api/events/update?id=" + id);
        }

        EventRequest eventRequest = JsonUtils.fromJson(requestBody, EventRequest.class);
        EventDto eventDto = EventMapper.toDto(existing);
        EventMapper.updateFromRequest(eventDto, eventRequest);
        EventModel updated = eventService.save(EventMapper.toModel(eventDto));
        EventDto finalDto = EventMapper.toDto(updated);

        auditLogService.log(request, AuditAction.UPDATE_EVENT, String.valueOf(finalDto.getId()), JsonUtils.toJson(finalDto));
        return ApiResponse.success(HttpStatus.OK, "Event updated.", "/api/events/update?id=" + id, finalDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(HttpServletRequest request, @RequestParam String id) {
        if (eventService.deleteById(id)) {
            auditLogService.log(request, AuditAction.DELETE_EVENT, id, null);
            return ApiResponse.success(HttpStatus.OK, "Event deleted.", "/api/events/delete?id=" + id, null);
        }
        return ApiResponse.error(HttpStatus.NOT_FOUND, "Event not found.", "/api/events/delete?id=" + id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'EMPLOYEE')")
    @GetMapping
    public ResponseEntity<?> getAll(HttpServletRequest request) {
        List<EventDto> events = EventMapper.toDto(eventService.findAll());
        List<Long> ids = events.stream().map(EventDto::getId).collect(Collectors.toList());
        auditLogService.log(request, AuditAction.GET_EVENTS, String.valueOf(events.size()), JsonUtils.toJson(ids));
        return ApiResponse.success(HttpStatus.OK, "Fetched all events.", "/api/events", events);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'EMPLOYEE')")
    @GetMapping("/search")
    public ResponseEntity<?> search(HttpServletRequest request, EventSearchRequest search) {
        if (Utils.notBlankString(search.getId())) {
            EventModel model = eventService.findById(search.getId());
            if (model == null) {
                return ApiResponse.error(HttpStatus.NOT_FOUND, "Event not found", "/api/events/search?id=" + search.getId());
            }
            EventDto dto = EventMapper.toDto(model);
            auditLogService.log(request, AuditAction.GET_EVENTS, "1", "[" + dto.getId() + "]");
            return ApiResponse.success(HttpStatus.OK, "Found event.", "/api/events/search?id=" + dto.getId(), dto);
        }

        List<EventDto> dtos = new ArrayList<>();
        if (search.getFrom() != null && search.getTo() != null) {
            List<EventModel> results = eventService.findByRange(search.getFrom(), search.getTo());
            dtos =  EventMapper.toDto(results);
            auditLogService.log(request, AuditAction.GET_EVENTS, String.valueOf(dtos.size()), JsonUtils.toJson(dtos.stream().map(EventDto::getId).toList()));

        }


        if(search.getEmployeeId() != null) {
            List<EventDto> goodDtos = new ArrayList<>();
            for(EventDto dto : dtos) {
                if(dto.getEmployeeId().equals(search.getEmployeeId())) {
                    goodDtos.add(dto);
                } else if (dto.getPeople().contains(search.getEmployeeId())) {
                    goodDtos.add(dto);
                }
            }
            dtos = goodDtos;
        }

        if(dtos.isEmpty()) {
            return ApiResponse.error(HttpStatus.NO_CONTENT, "Could not find events in each range or each error.", "/api/events/search");
        }
        return ApiResponse.success(HttpStatus.OK, "Found events", "/api/events/search", dtos);



    }
}
