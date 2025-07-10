package xyz.rugman27.drycleanerspos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import xyz.rugman27.drycleanerspos.security.JwtAuthenticationFilter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ALLOW all OPTIONS preflight
                        .requestMatchers("/api/employees/login", "/api/employees/photos/**", "/public/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler())
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date());
            error.put("status", 401);
            error.put("error", "Unauthorized");
            error.put("message", authException.getMessage());
            error.put("path", request.getRequestURI());

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), error);
        };
    }


    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {

            response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
            response.setHeader("Access-Control-Allow-Credentials", "true");


            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", new Date());
            error.put("status", 403);
            error.put("error", "Forbidden");
            error.put("message", accessDeniedException.getMessage());
            error.put("path", request.getRequestURI());

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), error);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // React dev server
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
