package xyz.rugman27.drycleanerspos.utilites;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.rugman27.drycleanerspos.dto.EmployeeDto;
import xyz.rugman27.drycleanerspos.mapper.EmployeeMapper;
import xyz.rugman27.drycleanerspos.service.EmployeeService;

public class AuthUtils {



    public static Claims getClaims(HttpServletRequest request) {
        return (Claims) request.getAttribute("claims");
    }

    public static String getEmployeeId(HttpServletRequest request) {
        Claims claims = getClaims(request);
        return claims != null ? claims.get("id", String.class) : null;
    }

    public static EmployeeDto getEmployeeDto(HttpServletRequest request, EmployeeService employeeService) {
        return EmployeeMapper.toDto(employeeService.getEmployeeById(getEmployeeId(request)));
    }

}
