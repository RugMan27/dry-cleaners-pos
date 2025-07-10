package xyz.rugman27.drycleanerspos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;
import xyz.rugman27.drycleanerspos.model.EventModel;
import xyz.rugman27.drycleanerspos.repository.EventRepository;

import java.util.List;

@Component
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public EventModel save(EventModel eventModel) {
        return eventRepository.save(eventModel);
    }
    public List<EventModel> findAll() {
        return eventRepository.findAll();
    }
    public List<EventModel> findByEmployee(String employeeId) {
        return eventRepository.findByEmployeeId(employeeId);
    }
    public List<EventModel> findByRange(String start, String end) {
        return eventRepository.findEventsInRange(start, end);
    }
    public EventModel findById(String id) {
        return eventRepository.findById(id).orElse(null);
    }
    public boolean deleteById(String id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }


}
