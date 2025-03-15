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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    PermissionService permissionService;

    @PostMapping("/module")
    ApiResponse<List<PermissionResponse>> createModuleForPermissions(
            @RequestBody @Valid CreateModuleRequest createModuleRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<List<PermissionResponse>>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Tạo mới module thành công")
                .data(this.permissionService.createModuleForPermissions(createModuleRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @DeleteMapping("/module/{name}")
    ApiResponse<Void> deleteModuleByName(@PathVariable("name") String moduleName, HttpServletRequest servletRequest) {
        this.permissionService.deleteModuleByName(moduleName);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Xóa module thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping("/modules")
    ApiResponse<List<String>> getAllModules(HttpServletRequest servletRequest) {
        return ApiResponse.<List<String>>builder()
                .statusCode(HttpStatus.OK.value())
                .data(this.permissionService.getAllModules())
                .message("Lấy thông tin toàn bộ tên module thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PostMapping
    ApiResponse<PermissionResponse> createPermission(
            @RequestBody @Valid CreatePermissionRequest createPermissionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Tạo mới quyền hạn thành công")
                .data(this.permissionService.createPermission(createPermissionRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<PermissionResponse> updatePermissionById(
            @RequestBody @Valid UpdatePermissionRequest updatePermissionRequest,
            @PathVariable("id") String permissionId, HttpServletRequest servletRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cập nhật thông tin quyền hạn thành công")
                .data(this.permissionService.updatePermissionById(permissionId, updatePermissionRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<PermissionResponse> getPermissionById(@PathVariable("id") String permissionId,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lấy thông tin một quyền hạn thành công")
                .data(this.permissionService.getPermissionById(permissionId))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllPermissionWithQuery(@Filter Specification<Permission> spec, Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lấy tất cả thông tin quyền hạn với điều kiện truy vấn thành công")
                .data(this.permissionService.getAllPermissionWithQuery(spec, pageable))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deletePermissionById(@PathVariable("id") String permissionId, HttpServletRequest servletRequest) {
        this.permissionService.deletePermissionById(permissionId);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Xóa quyền hạn thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }
}
