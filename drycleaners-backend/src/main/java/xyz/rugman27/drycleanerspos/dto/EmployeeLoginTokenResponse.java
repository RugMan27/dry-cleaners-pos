package xyz.rugman27.drycleanerspos.dto;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;
import xyz.rugman27.drycleanerspos.utilites.JwtUtils;

import java.util.Date;
import java.util.UUID;


@Getter
public class EmployeeLoginTokenResponse {
    public String token;
    public Long expiresAt;
    public String employeeId;
    public EmployeeLoginTokenResponse(EmployeeDto employee) {
        Date expireDate = new Date(System.currentTimeMillis() + JwtUtils.getEXPIRATION_TIME());
        this.expiresAt = expireDate.getTime();
        employee.getExtraData().setExpireTime(expiresAt);
        employee.getExtraData().setActive(true);
        employee.getExtraData().setLastLogin(System.currentTimeMillis());
        employee.getExtraData().setLoginCount(employee.getExtraData().getLoginCount()+1);
        this.employeeId = employee.getId();
        this.token = Jwts.builder()
                .setSubject(employee.getUsername())
                .claim("id", employee.getId())
                .claim("role", employee.getEmployeeType().toString())
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(JwtUtils.getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public EmployeeLoginTokenResponse() {
        long timestamp = System.currentTimeMillis();
        Date expireDate = new Date(timestamp + 10000);
        this.expiresAt = expireDate.getTime();
        String id = "TA-" + timestamp + "-" + UUID.randomUUID();
        this.token = Jwts.builder()
                .setSubject("Temporary Admin")
                .claim("id", id)
                .claim("role", "TEMPORARY_ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(JwtUtils.getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

}
