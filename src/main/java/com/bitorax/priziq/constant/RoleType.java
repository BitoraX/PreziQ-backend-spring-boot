package com.bitorax.priziq.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum RoleType {
    USER_ROLE("USER", "Người dùng có thể sử dụng các tính năng cơ bản"),
    ADMIN_ROLE("ADMIN", "Quản trị viên toàn quyền sử dụng hệ thống"),

    ;

    String name;
    String description;

    public static List<String> getAllRoleNames() {
        return Arrays.stream(values()).map(RoleType::getName).collect(Collectors.toList());
    }
}
