package xyz.rugman27.drycleanerspos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "events")
public class EventModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String start;
    private String end;
    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "calendar_id ")
    private String calendarId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata ;
}
