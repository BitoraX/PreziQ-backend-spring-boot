package com.bitorax.priziq.controller.rest;

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
import org.springframework.web.bind.annotation.*;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/roles")
public class RoleController {

    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> createRole(@RequestBody @Valid CreateRoleRequest createRoleRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<RoleResponse>builder()
                .message("Role created successfully")
                .data(roleService.createRole(createRoleRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{roleId}")
    ApiResponse<RoleResponse> getRoleById(@PathVariable String roleId, HttpServletRequest servletRequest) {
        return ApiResponse.<RoleResponse>builder()
                .message("Role retrieved successfully")
                .data(roleService.getRoleById(roleId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllRoleWithQuery(@Filter Specification<Role> spec, Pageable pageable, HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Roles retrieved successfully with query filters")
                .data(roleService.getAllRoleWithQuery(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{roleId}")
    ApiResponse<RoleResponse> updateRoleById(@PathVariable String roleId, @RequestBody UpdateRoleRequest updateRoleRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<RoleResponse>builder()
                .message("Role updated successfully")
                .data(roleService.updateRoleById(roleId, updateRoleRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{roleId}/permissions")
    ApiResponse<Void> deletePermissionFromRole(@PathVariable String roleId, @RequestBody DeletePermissionFromRoleRequest deletePermissionFromRoleRequest, HttpServletRequest servletRequest) {
        roleService.deletePermissionFromRole(roleId, deletePermissionFromRoleRequest);
        return ApiResponse.<Void>builder()
                .message("Permission removed from role successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{roleId}")
    ApiResponse<Void> deleteRoleById(@PathVariable String roleId, HttpServletRequest servletRequest) {
        roleService.deleteRoleById(roleId);
        return ApiResponse.<Void>builder()
                .message("Role deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
