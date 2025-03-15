package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.dto.request.role.CreateRoleRequest;
import com.bitorax.priziq.dto.request.role.UpdateRoleRequest;
import com.bitorax.priziq.dto.response.role.RoleResponse;
import com.bitorax.priziq.dto.response.role.RoleSecureResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role createRoleRequestToRole(CreateRoleRequest createRoleRequest);

    @Mapping(target = "permissions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoleRequestToRole(@MappingTarget Role role, UpdateRoleRequest updateRoleRequest);

    RoleSecureResponse roleToSecureResponse(Role role);

    RoleResponse roleToResponse(Role role);

    List<RoleResponse> rolesToRoleResponseList(List<Role> roles);
}
