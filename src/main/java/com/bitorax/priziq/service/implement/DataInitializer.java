package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.RoleType;
import com.bitorax.priziq.domain.Permission;
import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.AppException;
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
                        new Permission("Đăng ký tài khoản người dùng", "/api/v1/auth/register", "POST", "AUTH"),
                        new Permission("Xác thực tài khoản qua email", "/api/v1/auth/verify-active-account", "POST", "AUTH"),
                        new Permission("Gửi lại email để xác thực tài khoản", "/api/v1/auth/resend-verify", "POST", "AUTH"),
                        new Permission("Đăng nhập tài khoản người dùng", "/api/v1/auth/login", "POST", "AUTH"),
                        new Permission("Đăng xuất tài khoản người dùng", "/api/v1/auth/logout", "POST", "AUTH"),
                        new Permission("Lấy thông tin tài khoản người dùng", "/api/v1/auth/account", "GET", "AUTH"),
                        new Permission("Lấy thông tin bộ access/refresh token mới", "/api/v1/auth/refresh", "GET", "AUTH"),
                        new Permission("Quên mật khẩu tài khoản người dùng", "/api/v1/auth/forgot-password", "POST", "AUTH"),
                        new Permission("Đặt lại mật khẩu tài khoản người dùng", "/api/v1/auth/reset-password", "POST", "AUTH"),

                        // Module Users
                        new Permission("Cập nhật thông tin tài khoản người dùng", "/api/v1/users/update-profile", "PATCH", "USERS"),
                        new Permission("Cập nhật mật khẩu mới tài khoản người dùng", "/api/v1/users/update-password", "PUT", "USERS"),
                        new Permission("Cập nhật email mới tài khoản người dùng", "/api/v1/users/update-email", "PUT", "USERS"),
                        new Permission("Xác thực và cập nhật email mới tài khoản người dùng", "/api/v1/users/verify-change-email", "POST", "USERS"),
                        new Permission("Lấy thông tin tài khoản một người dùng", "/api/v1/users/{id}", "GET", "USERS"),
                        new Permission("Lấy thông tin tài khoản tất cả người dùng với điều kiện truy vấn", "/api/v1/users", "GET", "USERS"),
                        new Permission("Cập nhật thông tin tài khoản người dùng (dành cho admin)", "/api/v1/users/{id}", "PATCH", "USERS"),
                        new Permission("Xóa tài khoản một người dùng", "/api/v1/users/{id}", "DELETE", "USERS"),

                        // Module Roles
                        new Permission("Tạo mới một vai trò", "/api/v1/roles", "POST", "ROLES"),
                        new Permission("Cập nhật thông tin một vai trò (bao gồm thêm quyền hạn vào vai trò)", "/api/v1/roles/{id}", "PATCH", "ROLES"),
                        new Permission("Lấy thông tin một vai trò", "/api/v1/roles/{id}", "GET", "ROLES"),
                        new Permission("Lấy thông tin tất cả vai trò với điều kiện truy vấn", "/api/v1/roles", "GET", "ROLES"),
                        new Permission("Xóa quyền hạn khỏi một vai trò", "/api/v1/roles/{id}/permissions", "DELETE", "ROLES"),
                        new Permission("Xóa một vai trò", "/api/v1/roles/{id}", "DELETE", "ROLES"),

                        // Module Permissions
                        new Permission("Tạo mới một module", "/api/v1/permissions/module", "POST", "PERMISSIONS"),
                        new Permission("Xóa một module theo tên", "/api/v1/permissions/module/{name}", "DELETE", "PERMISSIONS"),
                        new Permission("Lấy thông tin tất cả tên module", "/api/v1/permissions/modules", "GET", "PERMISSIONS"),
                        new Permission("Tạo mới một quyền hạn", "/api/v1/permissions", "POST", "PERMISSIONS"),
                        new Permission("Cập nhật thông tin một quyền hạn", "/api/v1/permissions/{id}", "PATCH", "PERMISSIONS"),
                        new Permission("Lấy thông tin một quyền hạn", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"),
                        new Permission("Lấy thông tin tất cả quyền hạn với điều kiện truy vấn", "/api/v1/permissions", "GET", "PERMISSIONS"),
                        new Permission("Xóa một quyền hạn", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"),

                        // Module Files (AWS S3)
                        new Permission("Tải lên một file lên AWS S3", "/api/v1/storage/aws-s3/upload/single", "POST", "FILES"),
                        new Permission("Tải lên nhiều file lên AWS S3", "/api/v1/storage/aws-s3/upload/multiple", "POST", "FILES"),
                        new Permission("Xóa một file trên AWS S3", "/api/v1/storage/aws-s3/delete/single", "DELETE", "FILES"),
                        new Permission("Xóa nhiều file trên AWS S3", "/api/v1/storage/aws-s3/upload/multiple", "DELETE", "FILES"),
                        new Permission("Di chuyển một file từ một thư mục sang một thư mục mới", "/api/v1/storage/aws-s3/move/single", "PUT", "FILES"),
                        new Permission("Di chuyển nhiều file từ nhiều thư mục sang một thư mục mới", "/api/v1/storage/aws-s3/move/multiple", "PUT", "FILES"),

                        // Module Collections
                        new Permission("Tạo mới một bộ sưu tập", "/api/v1/collections", "POST", "COLLECTIONS")
                );
        }

        private Map<RoleType, List<Permission>> getDefaultRoles() {
                // Get all permissions of all modules
                List<Permission> moduleAuthAllPermissions = permissionRepository.findByModule("AUTH")
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleUserAllPermissions = permissionRepository.findByModule("USERS")
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleRoleAllPermissions = permissionRepository.findByModule("ROLES")
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> modulePermissionAllPermissions = permissionRepository.findByModule("PERMISSIONS")
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleFileAllPermissions = permissionRepository.findByModule("FILES")
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
                List<Permission> moduleCollectionAllPermissions = permissionRepository.findByModule("COLLECTIONS")
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));

                // Permission for role admin
                List<Permission> adminRolePermissions = combinePermissions(
                        List.of(
                                moduleAuthAllPermissions,
                                moduleUserAllPermissions,
                                moduleRoleAllPermissions,
                                modulePermissionAllPermissions,
                                moduleFileAllPermissions,
                                moduleCollectionAllPermissions
                        )
                );

                // Permissions for role user
                List<Permission> userSpecificPermissions = List.of(
                        findPermissionOrThrow("/api/v1/users/update-profile", "PATCH"),
                        findPermissionOrThrow("/api/v1/users/update-password", "PUT"),
                        findPermissionOrThrow("/api/v1/users/update-email", "PUT"),
                        findPermissionOrThrow("/api/v1/users/verify-change-email", "POST"),
                        findPermissionOrThrow("/api/v1/users/{id}", "GET"),
                        findPermissionOrThrow("/api/v1/users", "GET"),
                        findPermissionOrThrow("/api/v1/storage/aws-s3/upload/single", "POST"),
                        findPermissionOrThrow("/api/v1/storage/aws-s3/upload/multiple", "POST")
                );
                List<Permission> userRolePermissions = combinePermissions(List.of(moduleAuthAllPermissions, userSpecificPermissions));

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
                        .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
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
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NAME_NOT_FOUND));

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
