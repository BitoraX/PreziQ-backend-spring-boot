package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.dto.request.role.CreateRoleRequest;
import com.bitorax.priziq.dto.request.role.DeletePermissionFromRoleRequest;
import com.bitorax.priziq.dto.request.role.UpdateRoleRequest;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.role.RoleResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RoleService {
    RoleResponse createRole(CreateRoleRequest createRoleRequest);

    RoleResponse getRoleById(String roleId);

    PaginationResponse getAllRoleWithQuery(Specification<Role> spec, Pageable pageable);

    RoleResponse updateRoleById(String roleId, UpdateRoleRequest updateRoleRequest);

    void deletePermissionFromRole(String roleId, DeletePermissionFromRoleRequest deletePermissionFromRoleRequest);

    void deleteRoleById(String roleId);
}
