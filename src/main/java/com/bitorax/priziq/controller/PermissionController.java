package com.bitorax.priziq.controller;

import com.bitorax.priziq.domain.Permission;
import com.bitorax.priziq.dto.request.permission.CreateModuleRequest;
import com.bitorax.priziq.dto.request.permission.CreatePermissionRequest;
import com.bitorax.priziq.dto.request.permission.UpdatePermissionRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.permission.PermissionResponse;
import com.bitorax.priziq.service.PermissionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    PermissionService permissionService;

    @PostMapping("/module")
    ApiResponse<List<PermissionResponse>> createModuleForPermissions(@RequestBody @Valid CreateModuleRequest createModuleRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<List<PermissionResponse>>builder()
                .message("Module created successfully")
                .data(permissionService.createModuleForPermissions(createModuleRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/module/{name}")
    ApiResponse<Void> deleteModuleByName(@PathVariable("name") String moduleName, HttpServletRequest servletRequest) {
        permissionService.deleteModuleByName(moduleName);
        return ApiResponse.<Void>builder()
                .message("Module deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/modules")
    ApiResponse<List<String>> getAllModules(HttpServletRequest servletRequest) {
        return ApiResponse.<List<String>>builder()
                .message("All module names retrieved successfully")
                .data(permissionService.getAllModules())
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PostMapping
    ApiResponse<PermissionResponse> createPermission(@RequestBody @Valid CreatePermissionRequest createPermissionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .message("Permission created successfully")
                .data(permissionService.createPermission(createPermissionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<PermissionResponse> updatePermissionById(@RequestBody @Valid UpdatePermissionRequest updatePermissionRequest, @PathVariable("id") String permissionId, HttpServletRequest servletRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .message("Permission updated successfully")
                .data(permissionService.updatePermissionById(permissionId, updatePermissionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<PermissionResponse> getPermissionById(@PathVariable("id") String permissionId, HttpServletRequest servletRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .message("Permission retrieved successfully")
                .data(permissionService.getPermissionById(permissionId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllPermissionWithQuery(@Filter Specification<Permission> spec, Pageable pageable, HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Permissions retrieved successfully with query filters")
                .data(permissionService.getAllPermissionWithQuery(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deletePermissionById(@PathVariable("id") String permissionId, HttpServletRequest servletRequest) {
        permissionService.deletePermissionById(permissionId);
        return ApiResponse.<Void>builder()
                .message("Permission deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}