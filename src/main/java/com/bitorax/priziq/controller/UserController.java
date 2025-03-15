package com.bitorax.priziq.controller;

import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.dto.request.auth.VerifyEmailRequest;
import com.bitorax.priziq.dto.request.user.*;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.user.UserResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.bitorax.priziq.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/users")
public class UserController {
    UserService userService;

    @PatchMapping("/update-profile")
    ApiResponse<UserSecureResponse> updateUserProfile(
            @RequestBody @Valid UpdateUserProfileRequest updateUserProfileRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<UserSecureResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cập nhật thông tin người dùng thành công")
                .data(this.userService.updateUserProfile(updateUserProfileRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PutMapping("/update-password")
    ApiResponse<UserSecureResponse> updateUserPassword(
            @RequestBody @Valid UpdateUserPasswordRequest updateUserPasswordRequest,
            HttpServletRequest servletRequest) {
        return ApiResponse.<UserSecureResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cập nhật mật khẩu mới thành công")
                .data(this.userService.updateUserPassword(updateUserPasswordRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PutMapping("/update-email")
    ApiResponse<Void> updateUserEmail(@RequestBody @Valid UpdateUserEmailRequest updateUserEmailRequest,
            HttpServletRequest servletRequest) {
        this.userService.updateUserEmail(updateUserEmailRequest);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Vui lòng kiểm tra email mới để xác thực và hoàn tất cập nhật tài khoản của bạn")
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PostMapping("/verify-change-email")
    ApiResponse<UserSecureResponse> verifyEmailAndChangeNewEmail(
            @RequestBody @Valid VerifyEmailRequest verifyEmailRequest, HttpServletRequest servletRequest)
            throws ParseException, JOSEException {
        return ApiResponse.<UserSecureResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Thay đổi địa chỉ email mới thành công, vui lòng đăng nhập lại ")
                .data(this.userService.verifyEmailAndChangeNewEmail(verifyEmailRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllUserWithQuery(@Filter Specification<User> spec, Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lấy tất cả thông tin người dùng với điều kiện truy vấn thành công")
                .data(this.userService.getAllUserWithQuery(spec, pageable))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<Object> getUserById(@PathVariable("id") String userId, HttpServletRequest servletRequest) {
        return ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lấy thông tin thông tin người dùng thành công")
                .data(this.userService.getUserById(userId))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<UserResponse> updateUserForAdmin(@PathVariable("id") String userId,
            @RequestBody @Valid UpdateUserForAdminRequest updateUserForAdminRequest,
            HttpServletRequest servletRequest) {
        return ApiResponse.<UserResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cập nhật thông tin người dùng thành công")
                .data(this.userService.updateUserForAdmin(userId, updateUserForAdminRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> updateUserForAdmin(@PathVariable("id") String userId, HttpServletRequest servletRequest) {
        this.userService.deleteUserById(userId);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .message("Xóa tài khoản người dùng thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }
}
