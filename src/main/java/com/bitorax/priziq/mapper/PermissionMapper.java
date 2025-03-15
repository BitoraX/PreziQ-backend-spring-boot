package com.bitorax.priziq.mapper;

import com.bitorax.priziq.dto.request.permission.CreatePermissionRequest;
import com.bitorax.priziq.dto.request.permission.UpdatePermissionRequest;
import com.bitorax.priziq.dto.response.permission.PermissionResponse;
import com.bitorax.priziq.domain.Permission;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse permissionToResponse(Permission permission);

    Permission createPermissionRequestToPermission(CreatePermissionRequest createPermissionRequest);

    @Mapping(target = "module", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePermissionRequestToPermission(@MappingTarget Permission permission,
            UpdatePermissionRequest updatePermissionRequest);

    List<PermissionResponse> permissionsToPermissionResponseList(List<Permission> permissions);
}
