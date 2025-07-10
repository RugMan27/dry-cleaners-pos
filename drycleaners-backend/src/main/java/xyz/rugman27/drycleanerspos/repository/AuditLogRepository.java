package xyz.rugman27.drycleanerspos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.rugman27.drycleanerspos.model.AuditLogModel;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogModel, Long> {
}
