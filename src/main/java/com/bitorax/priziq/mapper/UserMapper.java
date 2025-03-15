package com.bitorax.priziq.mapper;

import com.bitorax.priziq.dto.request.auth.RegisterUserRequest;
import com.bitorax.priziq.dto.request.user.UpdateUserForAdminRequest;
import com.bitorax.priziq.dto.request.user.UpdateUserProfileRequest;
import com.bitorax.priziq.dto.response.user.UserResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.bitorax.priziq.domain.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {
    User registerRequestToUser(RegisterUserRequest registerUserRequest);

    @Mapping(target = "rolesSecured", source = "roles")
    UserSecureResponse userToSecureResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserProfileRequestToUser(@MappingTarget User user, UpdateUserProfileRequest updateUserProfileRequest);

    List<UserResponse> usersToUserResponseList(List<User> users);

    List<UserSecureResponse> usersToUserSecureResponseList(List<User> users);

    UserResponse userToResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserForAdminRequestToUser(@MappingTarget User user, UpdateUserForAdminRequest updateUserForAdminRequest);
}
