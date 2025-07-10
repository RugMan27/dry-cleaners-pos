package xyz.rugman27.drycleanerspos.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.rugman27.drycleanerspos.data.AuditAction;
import xyz.rugman27.drycleanerspos.model.AuditLogModel;
import xyz.rugman27.drycleanerspos.repository.AuditLogRepository;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(HttpServletRequest request, AuditAction action, String target, String detailsJson) {
        log(request, action, target, detailsJson, true);
    }

    public void log(HttpServletRequest request, AuditAction action, String target, String detailsJson, boolean success) {
        Claims claims;
        if(request == null) {
            claims = null;
        } else {
            claims = (Claims) request.getAttribute("claims");
        }
        AuditLogModel log = new AuditLogModel();
        if (claims == null) {
            System.out.println("claims is null");
            log.setEmployeeId("?");
            log.setAction(action);
            log.setTarget(target);
            log.setDetails(detailsJson);
            log.setSuccess(success);
            if(request != null) {
                log.setIpAddress(request.getRemoteAddr());
                log.setEndpoint(request.getRequestURI());
            }


            auditLogRepository.save(log);
            return;
        };

        log.setEmployeeId(claims.get("id", String.class));
        log.setAction(action);
        log.setTarget(target);
        log.setDetails(detailsJson);
        log.setIpAddress(request.getRemoteAddr());
        log.setSuccess(success);
        log.setEndpoint(request.getRequestURI());

        auditLogRepository.save(log);
    }
}
