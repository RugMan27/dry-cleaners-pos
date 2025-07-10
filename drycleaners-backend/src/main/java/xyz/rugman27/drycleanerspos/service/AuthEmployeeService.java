package xyz.rugman27.drycleanerspos.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.rugman27.drycleanerspos.dto.EmployeeDto;
import xyz.rugman27.drycleanerspos.mapper.EmployeeMapper;
import xyz.rugman27.drycleanerspos.repository.EmployeeRepository;

@Service
public class AuthEmployeeService {

    @Autowired
    private EmployeeService employeeService;

    public EmployeeDto getAuthenticatedEmployee(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) return null;

        String id = claims.get("id", String.class);
        return EmployeeMapper.toDto(employeeService.getEmployeeById(id));
    }
}
