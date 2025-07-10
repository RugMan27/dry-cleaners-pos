package xyz.rugman27.drycleanerspos.utilites;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import xyz.rugman27.drycleanerspos.dto.EmployeeDto;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;

import java.security.Key;
import java.util.Date;

public class JwtUtils {
    private static final String SECRET_KEY = EnvUtils.get("JWT_SECRET_KEY");
    @Getter
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1 day
    @Getter
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());


    public static Claims extractClaimsFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        String token = header.substring(7); // Remove "Bearer "

        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public static EmployeeModel.EmployeeType getEmployeeTypeFromClaims(Claims claims) {
        return  EmployeeModel.EmployeeType.valueOf((String) claims.get("role"));
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
