package xyz.rugman27.drycleanerspos.model;

import jakarta.persistence.*;
import lombok.Data;
import xyz.rugman27.drycleanerspos.data.AuditAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLogModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private AuditAction action;

    private String target;
    private String details;

    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean success;

    private String ipAddress;
    private String endpoint;
}
