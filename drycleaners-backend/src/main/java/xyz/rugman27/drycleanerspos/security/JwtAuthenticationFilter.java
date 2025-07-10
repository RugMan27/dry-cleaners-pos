package xyz.rugman27.drycleanerspos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.rugman27.drycleanerspos.dto.EmployeeDto;
import xyz.rugman27.drycleanerspos.mapper.EmployeeMapper;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;
import xyz.rugman27.drycleanerspos.repository.EmployeeRepository;
import xyz.rugman27.drycleanerspos.service.EmployeeService;
import xyz.rugman27.drycleanerspos.utilites.JwtUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final EmployeeService employeeService;

    public JwtAuthenticationFilter(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Claims claims = JwtUtils.extractClaimsFromRequest(request);

        if (claims == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid JWT token");
            return;
        }

        String employeeId = claims.get("id", String.class);

        // ✅ Optional: Check if user exists and is active
        EmployeeModel employee = employeeService.getEmployeeById(employeeId);
        EmployeeDto employeeDto = EmployeeMapper.toDto(employee);



        if (employee == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Employee doesn't exist\"}");
            return;
        }
        if (!employeeDto.isEnabled()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Employee is not enabled. Please contact a superior\"}");
            return;
        }
        if (!employeeDto.getExtraData().getActive()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Employee is not active. Please log in again.\"}");
            return;
        }


        // Attach claims to request (optional)
        request.setAttribute("claims", claims);

        // ✅ SET authentication BEFORE passing down the chain
        String username = claims.getSubject(); // usually the username
        String role = claims.get("role", String.class); // e.g., "ADMIN"

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)) // Spring expects ROLE_ prefix
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        // ✅ ONLY call doFilter ONCE, after setting the context
        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip filtering for public endpoints
        return path.equals("/api/employees/login") || path.startsWith("/public/") || path.startsWith("/api/employees/photos/");
    }


}
