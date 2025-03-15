package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.Permission;
import com.bitorax.priziq.dto.request.permission.CreateModuleRequest;
import com.bitorax.priziq.dto.request.permission.CreatePermissionRequest;
import com.bitorax.priziq.dto.request.permission.UpdatePermissionRequest;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.permission.PermissionResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PermissionService {
    List<PermissionResponse> createModuleForPermissions(CreateModuleRequest createModuleRequest);

    void deleteModuleByName(String moduleName);

    List<String> getAllModules();

    PermissionResponse createPermission(CreatePermissionRequest createPermissionRequest);

    PermissionResponse getPermissionById(String permissionId);

    PaginationResponse getAllPermissionWithQuery(Specification<Permission> spec, Pageable pageable);

    PermissionResponse updatePermissionById(String permissionId, UpdatePermissionRequest updatePermissionRequest);

    void deletePermissionById(String permissionId);
}
