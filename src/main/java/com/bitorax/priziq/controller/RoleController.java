package com.bitorax.priziq.controller;

import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.dto.request.role.CreateRoleRequest;
import com.bitorax.priziq.dto.request.role.DeletePermissionFromRoleRequest;
import com.bitorax.priziq.dto.request.role.UpdateRoleRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.role.RoleResponse;
import com.bitorax.priziq.service.RoleService;
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

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/roles")
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> createRole(@RequestBody @Valid CreateRoleRequest createRoleRequest,
            HttpServletRequest servletRequest) {
        return ApiResponse.<RoleResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Tạo mới vai trò thành công")
                .data(this.roleService.createRole(createRoleRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<RoleResponse> getRoleById(@PathVariable("id") String roleId, HttpServletRequest servletRequest) {
        return ApiResponse.<RoleResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lấy thông tin vai trò thành công")
                .data(this.roleService.getRoleById(roleId))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllRoleWithQuery(@Filter Specification<Role> spec, Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lấy thông tin tất cả vai trò với điều kiện truy vấn thành công")
                .data(this.roleService.getAllRoleWithQuery(spec, pageable))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<RoleResponse> updateRoleById(@PathVariable("id") String roleId,
            @RequestBody UpdateRoleRequest updateRoleRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<RoleResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cập nhật thông tin vai trò thành công")
                .data(this.roleService.updateRoleById(roleId, updateRoleRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @DeleteMapping("/{id}/permissions")
    ApiResponse<Void> deletePermissionFromRole(@PathVariable("id") String roleId,
            @RequestBody DeletePermissionFromRoleRequest deletePermissionFromRoleRequest,
            HttpServletRequest servletRequest) {
        this.roleService.deletePermissionFromRole(roleId, deletePermissionFromRoleRequest);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .message("Xóa quyền hạn khỏi vai trò thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteRoleById(@PathVariable("id") String roleId, HttpServletRequest servletRequest) {
        this.roleService.deleteRoleById(roleId);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .message("Xóa vai trò thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }
}
