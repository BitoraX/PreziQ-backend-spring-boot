package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.RoleType;
import com.bitorax.priziq.domain.Permission;
import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.repository.PermissionRepository;
import com.bitorax.priziq.repository.RoleRepository;
import com.bitorax.priziq.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInitializer implements ApplicationRunner {
        PasswordEncoder passwordEncoder;
        UserRepository userRepository;
        RoleRepository roleRepository;
        PermissionRepository permissionRepository;

        @NonFinal
        @Value("${priziq.account.base-password}")
        protected String BASE_PASSWORD;

        @NonFinal
        @Value("${priziq.allowed-init}")
        protected Boolean ALLOWED_INIT;

        @Override
        public void run(ApplicationArguments args) throws Exception {
                log.info(">>> START INIT DATA FOR DATABASE");

                if (isInitializationRequired()) {
                        initializePermissions(getDefaultPermissions());
                        initializeRoles(getDefaultRoles());
                        initializeUsers(getDefaultUsers());
                        log.info(">>> END INIT DATA FOR DATABASE");
                } else {
                        log.info(">>> SKIP INIT DATA FOR DATABASE");
                }
        }

        private boolean isInitializationRequired() {
                return ALLOWED_INIT && (userRepository.count() == 0 || roleRepository.count() == 0 || permissionRepository.count() == 0);
        }

        private void initializePermissions(List<Permission> permissions) {
                if (permissionRepository.count() == 0) {
                        permissionRepository.saveAll(permissions);
                }
        }

        private void initializeRoles(Map<RoleType, List<Permission>> roles) {
                if (roleRepository.count() == 0) {
                        List<Role> initRoles = new ArrayList<>();
                        for (Map.Entry<RoleType, List<Permission>> entry : roles.entrySet()) {
                                initRoles.add(
                                        Role.builder()
                                                .name(entry.getKey().getName())
                                                .description(entry.getKey().getDescription())
                                                .permissions(entry.getValue())
                                                .build()
                                );
                        }
                        roleRepository.saveAll(initRoles);
                }
        }

        private void initializeUsers(List<User> users) {
                if (userRepository.count() == 0) {
                        userRepository.saveAll(users);
                }
        }

        private List<Permission> getDefaultPermissions() {
                return List.of(
                        // Module Auth
                        new Permission("Register a user account", "/api/v1/auth/register", "POST", "AUTH"),
                        new Permission("Verify account via email", "/api/v1/auth/verify-active-account", "POST", "AUTH"),
                        new Permission("Resend verification email", "/api/v1/auth/resend-verify", "POST", "AUTH"),
                        new Permission("User login", "/api/v1/auth/login", "POST", "AUTH"),
                        new Permission("User logout", "/api/v1/auth/logout", "POST", "AUTH"),
                        new Permission("Retrieve user account information", "/api/v1/auth/account", "GET", "AUTH"),
                        new Permission("Retrieve new access/refresh tokens", "/api/v1/auth/refresh", "GET", "AUTH"),
                        new Permission("Forgot password", "/api/v1/auth/forgot-password", "POST", "AUTH"),
                        new Permission("Reset user password", "/api/v1/auth/reset-password", "POST", "AUTH"),

                        // Module Users
                        new Permission("Update user profile", "/api/v1/users/update-profile", "PATCH", "USERS"),
                        new Permission("Update user password", "/api/v1/users/update-password", "PUT", "USERS"),
                        new Permission("Update user email", "/api/v1/users/update-email", "PUT", "USERS"),
                        new Permission("Verify and update new email", "/api/v1/users/verify-change-email", "POST", "USERS"),
                        new Permission("Retrieve user account details", "/api/v1/users/{id}", "GET", "USERS"),
                        new Permission("Retrieve all user accounts with query parameters", "/api/v1/users", "GET", "USERS"),
                        new Permission("Update user account information (admin)", "/api/v1/users/{id}", "PATCH", "USERS"),
                        new Permission("Delete a user account", "/api/v1/users/{id}", "DELETE", "USERS"),
                        new Permission("Delete roles from user", "/api/v1/users/{id}/roles", "DELETE", "USERS"),

                        // Module Roles
                        new Permission("Create a new role", "/api/v1/roles", "POST", "ROLES"),
                        new Permission("Update role information (including adding permissions)", "/api/v1/roles/{id}", "PATCH", "ROLES"),
                        new Permission("Retrieve role information", "/api/v1/roles/{id}", "GET", "ROLES"),
                        new Permission("Retrieve all roles with query parameters", "/api/v1/roles", "GET", "ROLES"),
                        new Permission("Remove permissions from a role", "/api/v1/roles/{id}/permissions", "DELETE", "ROLES"),
                        new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"),

                        // Module Permissions
                        new Permission("Create a new module", "/api/v1/permissions/module", "POST", "PERMISSIONS"),
                        new Permission("Delete a module by name", "/api/v1/permissions/module/{name}", "DELETE", "PERMISSIONS"),
                        new Permission("Retrieve all module names", "/api/v1/permissions/modules", "GET", "PERMISSIONS"),
                        new Permission("Create a new permission", "/api/v1/permissions", "POST", "PERMISSIONS"),
                        new Permission("Update permission information", "/api/v1/permissions/{id}", "PATCH", "PERMISSIONS"),
                        new Permission("Retrieve permission information", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"),
                        new Permission("Retrieve all permissions with query parameters", "/api/v1/permissions", "GET", "PERMISSIONS"),
                        new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"),

                        // Module Files (AWS S3)
                        new Permission("Upload a file to AWS S3", "/api/v1/storage/aws-s3/upload/single", "POST", "FILES"),
                        new Permission("Upload multiple files to AWS S3", "/api/v1/storage/aws-s3/upload/multiple", "POST", "FILES"),
                        new Permission("Delete a file on AWS S3", "/api/v1/storage/aws-s3/delete/single", "DELETE", "FILES"),
                        new Permission("Delete multiple files on AWS S3", "/api/v1/storage/aws-s3/upload/multiple", "DELETE", "FILES"),
                        new Permission("Move a file from one folder to another", "/api/v1/storage/aws-s3/move/single", "PUT", "FILES"),
                        new Permission("Move multiple files from various folders to a new folder", "/api/v1/storage/aws-s3/move/multiple", "PUT", "FILES"),

                        // Module Collections
                        new Permission("Create a new collection", "/api/v1/collections", "POST", "COLLECTIONS"),
                        new Permission("Retrieve a collection", "/api/v1/collections/{id}", "GET", "COLLECTIONS"),
                        new Permission("Update collection information", "/api/v1/collections/{id}", "PATCH", "COLLECTIONS"),
                        new Permission("Retrieve all collections with query parameters", "/api/v1/collections", "GET", "COLLECTIONS"),
                        new Permission("Delete a collection", "/api/v1/collections/{id}", "DELETE", "COLLECTIONS"),
                        new Permission("Activities reorder", "/api/v1/collections/{id}/activities/reorder", "PUT", "COLLECTIONS"),
                        new Permission("Retrieve my collections with query parameters", "/api/v1/collections/me", "GET", "COLLECTIONS"),

                        // Module Activities
                        new Permission("Create a new activity", "/api/v1/activities", "POST", "ACTIVITIES"),
                        new Permission("Retrieve the list of activity types", "/api/v1/activities/types", "GET", "ACTIVITIES"),
                        new Permission("Update quiz for activity", "/api/v1/activities/{activityId}/quiz", "PUT", "ACTIVITIES"),
                        new Permission("Delete a activity", "/api/v1/activities/{activityId}", "DELETE", "ACTIVITIES"),
                        new Permission("Update a activity", "/api/v1/activities/{activityId}", "PUT", "ACTIVITIES"),
                        new Permission("Update a slide", "/api/v1/slides/{slideId}", "PUT", "ACTIVITIES"),
                        new Permission("Add a slide element", "/api/v1/slides/{slideId}/elements", "POST", "ACTIVITIES"),
                        new Permission("Update a slide element", "/api/v1/slides/{slideId}/elements/{elementId}", "PUT", "ACTIVITIES"),
                        new Permission("Delete a slide element", "/api/v1/slides/{slideId}/elements/{elementId}", "DELETE", "ACTIVITIES"),

                        // Module Sessions
                        new Permission("Create a new session", "/api/v1/sessions", "POST", "SESSIONS")
                );
        }

        private Map<RoleType, List<Permission>> getDefaultRoles() {
                // Get all permissions of modules
                List<Permission> moduleAuthAllPermissions = permissionRepository.findByModule("AUTH")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleUserAllPermissions = permissionRepository.findByModule("USERS")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleRoleAllPermissions = permissionRepository.findByModule("ROLES")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> modulePermissionAllPermissions = permissionRepository.findByModule("PERMISSIONS")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleFileAllPermissions = permissionRepository.findByModule("FILES")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleCollectionAllPermissions = permissionRepository.findByModule("COLLECTIONS")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleActivityAllPermissions = permissionRepository.findByModule("ACTIVITIES")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleSessionAllPermissions = permissionRepository.findByModule("SESSIONS")
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));

                // General permission for user login (USER, ADMIN)
                List<Permission> commonAuthenticatedPermissions = List.of(
                        findPermissionOrThrow("/api/v1/auth/logout", "POST"),
                        findPermissionOrThrow("/api/v1/auth/account", "GET"),
                        findPermissionOrThrow("/api/v1/users/update-profile", "PATCH"),
                        findPermissionOrThrow("/api/v1/users/update-password", "PUT"),
                        findPermissionOrThrow("/api/v1/users/update-email", "PUT"),
                        findPermissionOrThrow("/api/v1/users/verify-change-email", "POST"),
                        findPermissionOrThrow("/api/v1/users/{id}", "GET"),
                        findPermissionOrThrow("/api/v1/storage/aws-s3/upload/single", "POST"),
                        findPermissionOrThrow("/api/v1/storage/aws-s3/upload/multiple", "POST")
                );

                // Permission for ADMIN (entire system)
                List<Permission> adminRolePermissions = combinePermissions(
                        List.of(
                                moduleAuthAllPermissions,
                                moduleUserAllPermissions,
                                moduleRoleAllPermissions,
                                modulePermissionAllPermissions,
                                moduleFileAllPermissions,
                                moduleCollectionAllPermissions,
                                moduleActivityAllPermissions,
                                moduleSessionAllPermissions
                        )
                );

                // Permission for USER
                List<Permission> userRolePermissions = combinePermissions(
                        List.of(
                                moduleAuthAllPermissions,
                                commonAuthenticatedPermissions
                        )
                );

                return Map.of(
                        RoleType.ADMIN_ROLE, adminRolePermissions,
                        RoleType.USER_ROLE, userRolePermissions
                );
        }

        private List<Permission> combinePermissions(List<List<Permission>> permissionLists) {
                List<Permission> combined = new ArrayList<>();
                for (List<Permission> permissions : permissionLists)
                        combined.addAll(permissions);
                return combined.stream().distinct().toList();
        }

        private Permission findPermissionOrThrow(String apiPath, String httpMethod) {
                return permissionRepository.findByApiPathAndHttpMethod(apiPath, httpMethod)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_NOT_FOUND));
        }

        private List<User> getDefaultUsers() {
                return List.of(
                        createUser("priziq.admin@gmail.com", "Admin", "PriziQ", RoleType.ADMIN_ROLE),
                        createUser("priziq.user@gmail.com", "User", "PriziQ", RoleType.USER_ROLE),
                        createUser("thuanmobile1111@gmail.com", "Quách Phú", "Thuận", RoleType.ADMIN_ROLE),
                        createUser("thuyy566@gmail.com", "Lê Trần Hoàng", "Kiên", RoleType.ADMIN_ROLE),
                        createUser("tdmg1809@gmail.com", "Lê Phạm Thanh", "Duy", RoleType.ADMIN_ROLE),
                        createUser("tranquanmikaz@gmail.com", "Trần Nguyễn Minh", "Quân", RoleType.ADMIN_ROLE)
                );
        }

        private User createUser(String email, String firstName, String lastName, RoleType roleType) {
                Role role = roleRepository.findByName(roleType.getName())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NAME_NOT_FOUND));

                return User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(BASE_PASSWORD))
                        .firstName(firstName)
                        .lastName(lastName)
                        .isVerified(true)
                        .roles(List.of(role))
                        .build();
        }
}
