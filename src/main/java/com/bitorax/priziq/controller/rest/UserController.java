package com.bitorax.priziq.controller.rest;

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
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/users")
public class UserController {

    UserService userService;

    @PatchMapping("/update-profile")
    ApiResponse<UserSecureResponse> updateUserProfile(@RequestBody @Valid UpdateUserProfileRequest updateUserProfileRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<UserSecureResponse>builder()
                .message("User profile updated successfully")
                .data(userService.updateUserProfile(updateUserProfileRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PutMapping("/update-password")
    ApiResponse<UserSecureResponse> updateUserPassword(@RequestBody @Valid UpdateUserPasswordRequest updateUserPasswordRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<UserSecureResponse>builder()
                .message("Password updated successfully")
                .data(userService.updateUserPassword(updateUserPasswordRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PutMapping("/update-email")
    ApiResponse<Void> updateUserEmail(@RequestBody @Valid UpdateUserEmailRequest updateUserEmailRequest, HttpServletRequest servletRequest) {
        userService.updateUserEmail(updateUserEmailRequest);
        return ApiResponse.<Void>builder()
                .message("Please check your new email to verify and complete the update")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PostMapping("/verify-change-email")
    ApiResponse<UserSecureResponse> verifyEmailAndChangeNewEmail(@RequestBody @Valid VerifyEmailRequest verifyEmailRequest, HttpServletRequest servletRequest) throws ParseException, JOSEException {
        return ApiResponse.<UserSecureResponse>builder()
                .message("Email address changed successfully. Please log in again")
                .data(userService.verifyEmailAndChangeNewEmail(verifyEmailRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllUserWithQuery(@Filter Specification<User> spec, Pageable pageable, HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("All users retrieved successfully with query filters")
                .data(userService.getAllUserWithQuery(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserSecureResponse> getUserById(@PathVariable String userId, HttpServletRequest servletRequest) {
        return ApiResponse.<UserSecureResponse>builder()
                .message("User information retrieved successfully")
                .data(userService.getUserById(userId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{userId}")
    ApiResponse<UserResponse> updateUserForAdmin(@PathVariable String userId, @RequestBody @Valid UpdateUserForAdminRequest updateUserForAdminRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<UserResponse>builder()
                .message("User information updated successfully")
                .data(userService.updateUserForAdmin(userId, updateUserForAdminRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<Void> deleteUserById(@PathVariable String userId, HttpServletRequest servletRequest) {
        userService.deleteUserById(userId);
        return ApiResponse.<Void>builder()
                .message("User account deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{userId}/roles")
    ApiResponse<Void> deleteRoleFromUser(@PathVariable String userId, @RequestBody DeleteRoleFromUserRequest deleteRoleFromUserRequest, HttpServletRequest servletRequest) {
        userService.deleteRoleFromUser(userId, deleteRoleFromUserRequest);
        return ApiResponse.<Void>builder()
                .message("Xóa vai trò khỏi người dùng thành công")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}