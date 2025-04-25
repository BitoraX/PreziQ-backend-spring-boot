package com.bitorax.priziq.utils;

import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleUtils {
    RoleRepository roleRepository;

    public Set<String> getRoleIdsFromUser(User user) {
        return user.getRoles().stream()
                .map(Role::getRoleId)
                .collect(Collectors.toSet());
    }

    public void checkDuplicateRoleIds(List<String> roleIds) {
        Set<String> uniqueRoleIds = new HashSet<>(roleIds);
        if (uniqueRoleIds.size() < roleIds.size()) {
            throw new ApplicationException(ErrorCode.DUPLICATE_ROLE_IDS);
        }
    }

    public void validateUserDoesNotAlreadyHaveRoles(User user, List<Role> newRoles) {
        Set<String> currentRoleIds = user.getRoles().stream()
                .map(Role::getRoleId)
                .collect(Collectors.toSet());

        Set<String> duplicateRoles = newRoles.stream()
                .map(Role::getRoleId)
                .filter(currentRoleIds::contains)
                .collect(Collectors.toSet());

        if (!duplicateRoles.isEmpty()) {
            String errorMessage = "Người dùng đã có vai trò với ID: " + duplicateRoles;
            throw new ApplicationException(ErrorCode.ROLE_ALREADY_ASSIGNED, errorMessage);
        }
    }

    public List<Role> validateRolesExist(List<String> providedIds) {
        List<Role> existingRoles = this.roleRepository.findAllById(providedIds);

        Set<String> existingIds = existingRoles.stream()
                .map(Role::getRoleId)
                .collect(Collectors.toSet());

        Set<String> nonExistentIds = providedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toSet());

        if (!nonExistentIds.isEmpty()) {
            String customErrorMessage = "Vai trò với ID: " + nonExistentIds + " không tồn tại trên hệ thống";
            throw new ApplicationException(ErrorCode.ROLE_NOT_FOUND, customErrorMessage);
        }

        return existingRoles;
    }
}
