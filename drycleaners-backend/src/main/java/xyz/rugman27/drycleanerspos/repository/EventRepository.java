package xyz.rugman27.drycleanerspos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.rugman27.drycleanerspos.model.EventModel;

import java.util.List;

public interface EventRepository  extends JpaRepository<EventModel, String> {
    @Query("SELECT e FROM EventModel e WHERE e.start <= :rangeEnd AND e.end >= :rangeStart")
    List<EventModel> findEventsInRange(
            @Param("rangeStart") String rangeStart,
            @Param("rangeEnd") String rangeEnd);

    List <EventModel> findByEmployeeId(String employeeId);
}
