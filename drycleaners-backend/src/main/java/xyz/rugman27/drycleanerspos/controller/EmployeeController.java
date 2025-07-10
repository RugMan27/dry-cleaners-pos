package xyz.rugman27.drycleanerspos.controller;

import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import xyz.rugman27.drycleanerspos.data.AuditAction;
import xyz.rugman27.drycleanerspos.dto.*;

import xyz.rugman27.drycleanerspos.mapper.EmployeeMapper;
import xyz.rugman27.drycleanerspos.model.EmployeeModel;

import xyz.rugman27.drycleanerspos.service.AuditLogService;
import xyz.rugman27.drycleanerspos.service.AuthEmployeeService;
import xyz.rugman27.drycleanerspos.service.EmployeeService;
import xyz.rugman27.drycleanerspos.utilites.*;

import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {


    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuthEmployeeService authEmployeeService;

    @Autowired
    private AuditLogService auditLogService;

    @PostConstruct
    public void init() {
        try {
            System.out.println("⏳ Fetching employees...");
            List<EmployeeDto> employees = EmployeeMapper.toDto(employeeService.getAllEmployees());

            System.out.println("✅ Employees fetched: " + employees.size());
            boolean oneGoodEmployee = false;

            for (EmployeeDto employee : employees) {
                if (employee.isEnabled()) oneGoodEmployee = true;

                if (employee.getExtraData() != null && employee.getExtraData().getExpireTime() == null) {
                    employee.getExtraData().setExpireTime(0L);
                }

                setEmployeeStatus(employee);
            }

            if (employees.isEmpty() || !oneGoodEmployee) {
                System.out.println("🟡 No enabled employees found, generating default login token...");
                System.out.println(JsonUtils.toJson(new EmployeeLoginTokenResponse()));
            }
        } catch (Exception e) {
            System.err.println("🔥 Failed during init: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'EMPLOYEE')")
    public boolean setEmployeeStatus(EmployeeDto employee) {
        if(employee.getExtraData() != null && employee.getExtraData().getActive() != null){
            if(employee.getExtraData().getActive() != null && employee.getExtraData().getActive() && employee.getExtraData().getExpireTime()>System.currentTimeMillis()) {
                employee.getExtraData().setActive(true);
                employeeService.saveEmployee(EmployeeMapper.toModel(employee));
                Long waitTime = employee.getExtraData().getExpireTime();
                System.out.println("Started logout thread for '" + employee.getId() + "' in " + String.valueOf(waitTime) + "ms.");
                ThreadUtil.runDelayed(() -> {
                    setEmployeeStatus(employee);

                }, waitTime);
                return true;
            }
        }
        employee.getExtraData().setActive(false);
        employeeService.saveEmployee(EmployeeMapper.toModel(employee));
        auditLogService.log(null, AuditAction.LOGOUT, employee.getId(), "{\"message\":\"System Logout\"}");
        return false;


    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'TEMPORARY_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<? > create(HttpServletRequest request, @RequestBody String requestBody) {
        EmployeeRequest employeeRequest = JsonUtils.fromJson(requestBody, EmployeeRequest.class);

        if(Utils.notBlankString(employeeRequest.getLastName()) && Utils.notBlankString(employeeRequest.getFirstName()) && Utils.notBlankString(employeeRequest.getPassword())) {
            EmployeeModel employeeModel = employeeService.createEmployeeFromLastName(employeeRequest.getLastName(), employeeRequest.getFirstName(), employeeRequest.getPassword());
            EmployeeDto employeeDto = EmployeeMapper.toDto(employeeModel);
            EmployeeMapper.updateFromRequest(employeeDto, employeeRequest);
            employeeDto.setEnabled(true);

            EmployeeModel finalEmployeeModel =  employeeService.saveEmployee(EmployeeMapper.toModel(employeeDto));
            if(finalEmployeeModel.isEnabled()){
                EmployeeDto finalEmployeeDto = EmployeeMapper.toDto(finalEmployeeModel);
                auditLogService.log(request, AuditAction.CREATE_EMPLOYEE, finalEmployeeDto.getId(), JsonUtils.toJson(finalEmployeeDto));
                return ApiResponse.success(HttpStatus.OK, "Employee created successfully.","/api/employees/create", finalEmployeeDto);

            }else {
                return ApiResponse.error(HttpStatus.OK, "Employee created with modification.","/api/employees/create", EmployeeMapper.toDto(finalEmployeeModel));

            }
        } else {
        return ApiResponse.error(
                HttpStatus.BAD_REQUEST,
                "No last name provided",
                "/api/employees/create"
        );
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'EMPLOYEE')")
    @PutMapping("/update")
    public ResponseEntity<?> update(HttpServletRequest request, @RequestBody String requestBody,@RequestParam String id) {
        EmployeeDto requestingEmployeeDto = AuthUtils.getEmployeeDto(request, employeeService);
        if(
            requestingEmployeeDto.getEmployeeType() != EmployeeModel.EmployeeType.MANAGER &&
            requestingEmployeeDto.getEmployeeType() != EmployeeModel.EmployeeType.ADMIN &&
            requestingEmployeeDto.getEmployeeType() != EmployeeModel.EmployeeType.OWNER
        ) {
            if(!requestingEmployeeDto.getId().equals(id)) {
                return ApiResponse.error(
                        HttpStatus.UNAUTHORIZED,
                        "You do not have the role to modify other employees",
                        "/api/employees/update?id=" + id
                );
            }
        }

        if(Utils.notBlankString(id)){
            EmployeeRequest employeeRequest = JsonUtils.fromJson(requestBody, EmployeeRequest.class);

            EmployeeModel employeeModel  = employeeService.getEmployeeById(id);
            if(employeeModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "employee '"+id+"' not found",
                        "/api/employees/update?id=" + id
                );
            }
            EmployeeDto employeeDto = EmployeeMapper.toDto(employeeModel);

            System.out.println(JsonUtils.toJson(employeeRequest));

            EmployeeMapper.updateFromRequest(employeeDto, employeeRequest);

            System.out.println(employeeDto.getProfilePicBase64());

            EmployeeModel finalEmployeeModel =  employeeService.saveEmployee(EmployeeMapper.toModel(employeeDto));
            if(finalEmployeeModel.isEnabled()){
                EmployeeDto finalEmployeeDto = EmployeeMapper.toDto(finalEmployeeModel);
                auditLogService.log(request, AuditAction.UPDATE_EMPLOYEE, finalEmployeeDto.getId(), JsonUtils.toJson(finalEmployeeDto));
                return ApiResponse.success(HttpStatus.OK, "Employee updated successfully.","/api/employees/update?id="+id, finalEmployeeDto);

            }else {
                return ApiResponse.error(HttpStatus.OK, "Employee updated with modification.","/api/employees/update?id="+id, EmployeeMapper.toDto(finalEmployeeModel));
            }
        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank ID",
                    "/api/employees/update?id=" + id
            );
        }

    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'TEMPORARY_ADMIN')")
    @PutMapping("/enable")
    public ResponseEntity<?> enable(HttpServletRequest request,@RequestParam String id) {
        if(Utils.notBlankString(id)){
            EmployeeModel employeeModel  = employeeService.getEmployeeById(id);
            if(employeeModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "employee '"+id+"' not found",
                        "/api/employees/enable?id=" + id
                );
            }
            EmployeeDto employeeDto = EmployeeMapper.toDto(employeeModel);

            employeeDto.setEnabled(true);

            employeeService.saveEmployee(EmployeeMapper.toModel(employeeDto));


            auditLogService.log(request, AuditAction.ENABLE_EMPLOYEE, employeeDto.getId(), JsonUtils.toJson(employeeDto));
            return ApiResponse.success(HttpStatus.OK, "Employee enabled successfully.","/api/employees/enable?id="+id, employeeDto);

        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank ID",
                    "/api/employees/enable?id=" + id
            );
        }

    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @PutMapping("/disable")
    public ResponseEntity<?> disable(HttpServletRequest request,@RequestParam String id) {
        if(Utils.notBlankString(id)){
            EmployeeModel employeeModel  = employeeService.getEmployeeById(id);
            if(employeeModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "employee '"+id+"' not found",
                        "/api/employees/disable?id=" + id
                );
            }
            EmployeeDto employeeDto = EmployeeMapper.toDto(employeeModel);

            employeeDto.setEnabled(false);

            EmployeeModel finalEmployeeModel = employeeService.saveEmployee(EmployeeMapper.toModel(employeeDto));

            EmployeeDto finalEmployeeDto = EmployeeMapper.toDto(finalEmployeeModel);

            auditLogService.log(request, AuditAction.DISABLE_EMPLOYEE, employeeDto.getId(), JsonUtils.toJson(finalEmployeeDto));
            return ApiResponse.success(HttpStatus.OK, "Employee enabled successfully.","/api/employees/disable?id="+id, finalEmployeeDto);

        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank ID",
                    "/api/employees/disable?id=" + id
            );
        }

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'EMPLOYEE', 'TEMPORARY_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllemployees(HttpServletRequest request) {
        List<EmployeeDto> employeeDtoList = EmployeeMapper.toDto(employeeService.getAllEmployees());
        List<String> ids = employeeDtoList.stream()
                .map(EmployeeDto::getId)
                .collect(Collectors.toList());
        auditLogService.log(request, AuditAction.GET_EMPLOYEES, String.valueOf(employeeDtoList.size()), JsonUtils.toJson(ids));
        return ApiResponse.success(HttpStatus.OK,"Got all employees.", "/api/employees", employeeDtoList);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> searchemployees(HttpServletRequest request, @RequestParam String id){
        if(employeeService.deleteEmployeeById(id)){
            auditLogService.log(request, AuditAction.DELETE_EMPLOYEE, id, null);
            return ApiResponse.success(HttpStatus.OK,"Deleted user '"+id+"'", "/api/employees/delete?id=" + id, null);
        }
        return ApiResponse.error(HttpStatus.NOT_FOUND,"Could not delete user user '"+id+"'", "/api/employees/delete?id=" + id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'EMPLOYEE')")
    @GetMapping("/search")
    public ResponseEntity<?> searchEmployees(HttpServletRequest request, EmployeeSearchRequest search) {

        if (search.getId() != null && !search.getId().isBlank()) {
            EmployeeModel employeeModel = employeeService.getEmployeeById(search.getId());
            if (employeeModel != null) {
                EmployeeDto dto = EmployeeMapper.toDto(employeeModel);
                auditLogService.log(request, AuditAction.GET_EMPLOYEES, "1", "[" + dto.getId() + "]");
                return ApiResponse.success(HttpStatus.OK, "Employee Found.", "/api/employees/search?id=" + dto.getId(), dto);
            }
            return ApiResponse.error(HttpStatus.NOT_FOUND, "No employee with ID '" + search.getId() + "' found.", "/api/employees/search?id=" + search.getId());
        }

        if (search.getUser() != null && !search.getUser().isBlank()) {
            EmployeeModel employeeModel = employeeService.getEmployeeByUsername(search.getUser());
            if (employeeModel != null) {
                EmployeeDto dto = EmployeeMapper.toDto(employeeModel);
                auditLogService.log(request, AuditAction.GET_EMPLOYEES, "1", "[" + dto.getId() + "]");
                return ApiResponse.success(HttpStatus.OK, "Employee Found.", "/api/employees/search?user=" + search.getUser(), dto);
            }
            return ApiResponse.error(HttpStatus.NOT_FOUND, "No employee with username '" + search.getUser() + "' found.", "/api/employees/search?user=" + search.getUser());
        }

        if (search.getLast() != null && !search.getLast().isBlank()) {
            List<EmployeeModel> employees = employeeService.searchEmployeeByLastName(search.getLast());
            List<EmployeeDto> dtos = EmployeeMapper.toDto(employees);

            if (!dtos.isEmpty()) {
                List<String> ids = dtos.stream().map(EmployeeDto::getId).toList();
                auditLogService.log(request, AuditAction.GET_EMPLOYEES, String.valueOf(dtos.size()), JsonUtils.toJson(ids));
                return ApiResponse.success(HttpStatus.OK, "Found employee(s) with last name '" + search.getLast() + "'", "/api/employees/search?last=" + search.getLast(), dtos);
            }
            return ApiResponse.error(HttpStatus.NOT_FOUND, "No employee with last name like '" + search.getLast() + "' found.", "/api/employees/search?last=" + search.getLast());
        }

        return ApiResponse.error(HttpStatus.BAD_REQUEST, "Not a valid search", "/api/employees/search");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'EMPLOYEE')")
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, @RequestBody String requestBody){
        EmployeeLoginDto employeeLoginDto = JsonUtils.fromJson(requestBody,EmployeeLoginDto.class);
        String username =  employeeLoginDto.getUsername();
        String password = employeeLoginDto.getPassword();

        EmployeeDto employee = EmployeeMapper.toDto(employeeService.getEmployeeByUsername(username));

        if(employee ==null || employee.getOriginalModel() == null){
            auditLogService.log(request, AuditAction.FAILED_LOGIN, username, "Does user not exist");
            return ApiResponse.error(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password.",
                    "/api/employees/login"
            );
        }
        if(PasswordUtils.checkPassword(password,employee.getPasswordHash())){
            EmployeeLoginTokenResponse token = new EmployeeLoginTokenResponse(employee);
            employeeService.saveEmployee(EmployeeMapper.toModel(employee));
            request.setAttribute("claims", JwtUtils.parseToken(token.getToken()));
            auditLogService.log(request, AuditAction.LOGIN, employee.getId(), null);

            return ApiResponse.success(HttpStatus.OK,"Successfully logged in '" + username + "'", "/api/employees/login", token);
        } else {
            auditLogService.log(request, AuditAction.FAILED_LOGIN, username, "Bad password");
            return ApiResponse.error(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password.",
                    "/api/employees/login"
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        Claims claims = JwtUtils.extractClaimsFromRequest(request);
        if(claims == null) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST,"Missing or invalid Authorization header.", "/api/employees/logout");
        }
        String employeeId = claims.get("id", String.class);

        EmployeeModel employeeModel = employeeService.getEmployeeById(employeeId);
        if (employeeModel == null) return ApiResponse.error(HttpStatus.NOT_FOUND,"Employee not found.", "/api/employees/logout");

        EmployeeDto employee = EmployeeMapper.toDto(employeeModel);

        employee.getExtraData().setActive(false);

        employeeService.saveEmployee(EmployeeMapper.toModel(employee));
        auditLogService.log(request, AuditAction.LOGOUT, employeeId, null);
        return ApiResponse.success(HttpStatus.OK,"Logged out.", "/api/employees/logout", null);

    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        EmployeeDto dto = authEmployeeService.getAuthenticatedEmployee(request);

        if (dto == null) {
            return ApiResponse.error(HttpStatus.UNAUTHORIZED,   "You dont exist.", "/api/employees/me");
        }



        return ApiResponse.success(HttpStatus.OK, "Found your data", "/api/employees/me", dto);
    }
    @GetMapping("/photos/{uuid}.jpg")
    public ResponseEntity<byte[]> getMePhoto(HttpServletRequest request, @PathVariable String uuid) {
        EmployeeModel model;

        model = employeeService.getEmployeeByPhotoUuid(uuid);


        String base64String = model.getProfilePic();  // assuming it's a String with Base64 content
        byte[] imageBytes = Base64.getDecoder().decode(base64String); // This should return raw bytes, NOT base64
        if (imageBytes == null) {
            return null;
        }

        String contentType = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            contentType = URLConnection.guessContentTypeFromStream(bais);
        } catch (Exception e) {
            // handle or log error if needed
        }

        if (contentType == null) {
            contentType = "image/jpeg";
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(imageBytes);
    }




}
