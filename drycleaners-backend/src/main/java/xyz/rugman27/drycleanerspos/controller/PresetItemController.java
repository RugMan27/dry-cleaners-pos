package xyz.rugman27.drycleanerspos.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.rugman27.drycleanerspos.data.AuditAction;
import xyz.rugman27.drycleanerspos.data.ServiceType;
import xyz.rugman27.drycleanerspos.dto.PresetItemRequest;
import xyz.rugman27.drycleanerspos.mapper.PresetItemMapper;
import xyz.rugman27.drycleanerspos.model.PresetItemModel;
import xyz.rugman27.drycleanerspos.service.AuditLogService;
import xyz.rugman27.drycleanerspos.service.PresetItemService;
import xyz.rugman27.drycleanerspos.utilites.ApiResponse;
import xyz.rugman27.drycleanerspos.utilites.AuthUtils;
import xyz.rugman27.drycleanerspos.utilites.JsonUtils;
import xyz.rugman27.drycleanerspos.utilites.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/preset-items")
public class PresetItemController {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private PresetItemService presetItemService;
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @PostMapping("/create")
    public ResponseEntity<? > create(HttpServletRequest request, @RequestBody String requestBody) {
        PresetItemRequest presetItemRequest = JsonUtils.fromJson(requestBody, PresetItemRequest.class);

        if(presetItemRequest != null && Utils.notBlankString(presetItemRequest.getCode())) {

            if(presetItemService.getByCode(presetItemRequest.getCode()) != null) {
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "No code already exists",
                        "/api/preset-items/create"
                );
            }


            PresetItemModel presetItemModel = PresetItemMapper.fromRequest(presetItemRequest);

            presetItemService.savePresetItem(presetItemModel);
            auditLogService.log(request, AuditAction.CREATE_PRESET_ITEM, presetItemModel.getCode(), JsonUtils.toJson(presetItemModel));
            return ApiResponse.success(HttpStatus.OK, "Preset Item created successfully.","/api/preset-items/create", presetItemModel);


        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "No code provided",
                    "/api/preset-items/create"
            );
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @PutMapping("/update")
    public ResponseEntity<?> update(HttpServletRequest request, @RequestBody String requestBody,@RequestParam String code) {

        if(Utils.notBlankString(code)){
            PresetItemRequest presetItemRequest = JsonUtils.fromJson(requestBody, PresetItemRequest.class);

            PresetItemModel presetItemModel  = presetItemService.getByCode(code);
            if(presetItemModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "Preset Item '"+code+"' not found",
                        "/api/preset-items/update?code=" + code
                );
            }

            PresetItemMapper.updateFromRequest(presetItemModel, presetItemRequest);

            presetItemService.savePresetItem(presetItemModel);

            auditLogService.log(request, AuditAction.CREATE_PRESET_ITEM, presetItemModel.getCode(), JsonUtils.toJson(presetItemModel));
            return ApiResponse.success(HttpStatus.OK, "Preset Item updated successfully.","/api/preset-items/update?code="+code, presetItemModel);


        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank code",
                    "/api/preset-items/update?id=" + code
            );
        }

    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @PutMapping("/enable")
    public ResponseEntity<?> enable(HttpServletRequest request,@RequestParam String code) {
        if(Utils.notBlankString(code)){
            PresetItemModel presetItemModel  = presetItemService.getByCode(code);
            if(presetItemModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "Preset Item '"+code+"' not found",
                        "/api/preset-items/enable?code=" + code
                );
            }


            presetItemModel.setEnabled(true);

            presetItemService.savePresetItem(presetItemModel);


            auditLogService.log(request, AuditAction.UPDATE_PRESET_ITEM, presetItemModel.getCode(), JsonUtils.toJson(presetItemModel));
            return ApiResponse.success(HttpStatus.OK, "Preset Item enabled successfully.","/api/preset-items/enable?code="+code, presetItemModel);

        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank ID",
                    "/api/preset-items/enable?code=" + code
            );
        }

    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @PutMapping("/disable")
    public ResponseEntity<?> disable(HttpServletRequest request,@RequestParam String code) {
        if(Utils.notBlankString(code)){
            PresetItemModel presetItemModel  = presetItemService.getByCode(code);
            if(presetItemModel == null){
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "preset Item '"+code+"' not found",
                        "/api/preset-items/disable?code=" + code
                );
            }


            presetItemModel.setEnabled(false);

            presetItemService.savePresetItem(presetItemModel);


            auditLogService.log(request, AuditAction.UPDATE_PRESET_ITEM, presetItemModel.getCode(), JsonUtils.toJson(presetItemModel));
            return ApiResponse.success(HttpStatus.OK, "Preset Item disabled successfully.","/api/preset-items/enable?code="+code, presetItemModel);

        } else {
            return ApiResponse.error(
                    HttpStatus.BAD_REQUEST,
                    "Blank ID",
                    "/api/preset-items/disable?code=" + code
            );
        }

    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK', 'EMPLOYEE')")
    @GetMapping
    public ResponseEntity<?> getAllPresetItems(HttpServletRequest request) {
        List<PresetItemModel> presetItemModelList = presetItemService.getAll();
        List<String> ids = presetItemModelList.stream()
                .map(PresetItemModel::getCode)
                .collect(Collectors.toList());
        auditLogService.log(request, AuditAction.GET_PRESET_ITEMS, String.valueOf(presetItemModelList.size()), JsonUtils.toJson(ids));
        return ApiResponse.success(HttpStatus.OK,"Got all Preset Items.", "/api/preset-items", presetItemModelList);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> searchPresetItems(HttpServletRequest request, @RequestParam String code){
        if(presetItemService.deleteByCode(code)){
            auditLogService.log(request, AuditAction.DELETE_PRESET_ITEM, code, null);
            return ApiResponse.success(HttpStatus.OK,"Deleted Preset Item '"+code+"'", "/api/preset-items/delete?id=" + code, null);
        }
        return ApiResponse.error(HttpStatus.NOT_FOUND,"Could not delete Preset Item '"+code+"'", "/api/preset-items/delete?id=" + code);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OWNER', 'CLERK', 'EMPLOYEE')")
    @GetMapping("/search")
    public ResponseEntity<?> searchPresetItems(
            HttpServletRequest request,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String type
    ) {

        if (code != null && !code.isBlank()) {
            PresetItemModel presetItemModel = presetItemService.getByCode(code);
            if (presetItemModel != null) {
                auditLogService.log(request, AuditAction.GET_PRESET_ITEMS, "1", "[" + presetItemModel.getCode() + "]");
                return ApiResponse.success(
                        HttpStatus.OK,
                        "Preset Item Found.",
                        "/api/preset-items/search?code=" + code,
                        presetItemModel
                );
            } else {
                return ApiResponse.error(
                        HttpStatus.NOT_FOUND,
                        "No Preset Item with code '" + code + "' found.",
                        "/api/preset-items/search?code=" + code
                );
            }
        }

        if (type != null && !type.isBlank()) {
            ServiceType serviceType = ServiceType.safeParse(type);
            if (serviceType == null) {
                return ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        "Invalid service type '" + type + "'.",
                        "/api/preset-items/search?type=" + type
                );
            }

            List<PresetItemModel> items = presetItemService.getAllByServiceType(serviceType);
            if (items != null && !items.isEmpty()) {
                List<String> ids = items.stream().map(PresetItemModel::getCode).toList();
                auditLogService.log(request, AuditAction.GET_PRESET_ITEMS, String.valueOf(items.size()), JsonUtils.toJson(ids));
                return ApiResponse.success(
                        HttpStatus.OK,
                        "Found Preset Items with service type '" + type + "'.",
                        "/api/preset-items/search?type=" + type,
                        items
                );
            } else {
                return ApiResponse.error(
                        HttpStatus.NOT_FOUND,
                        "No Preset Items found for type '" + type + "'.",
                        "/api/preset-items/search?type=" + type
                );
            }
        }

        return ApiResponse.error(
                HttpStatus.BAD_REQUEST,
                "Not a valid search. Provide either `code` or `type`.",
                "/api/preset-items/search"
        );
    }


}
