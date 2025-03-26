package com.bitorax.priziq.controller;

import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.request.auth.*;
import com.bitorax.priziq.dto.response.auth.AuthenticationResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.bitorax.priziq.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

        AuthenticationService authenticationService;

        @PostMapping("/register")
        ApiResponse<Void> register(@RequestBody @Valid RegisterUserRequest registerUserRequest, HttpServletRequest servletRequest) {
                authenticationService.register(registerUserRequest);
                return ApiResponse.<Void>builder()
                        .message("Please check your email to verify your account")
                        .meta(buildMetaInfo(servletRequest))
                        .build();
        }

        @PostMapping("/verify-active-account")
        ApiResponse<AuthenticationResponse> verifyEmailAndActivateAccount(@RequestBody @Valid VerifyEmailRequest verifyEmailRequest, HttpServletRequest servletRequest) throws ParseException, JOSEException {
                return ApiResponse.<AuthenticationResponse>builder()
                        .message("Your email has been successfully verified")
                        .data(authenticationService.verifyEmailAndActivateAccount(verifyEmailRequest))
                        .meta(buildMetaInfo(servletRequest))
                        .build();
        }

        @PostMapping("/resend-verify")
        ApiResponse<Void> resendVerifyEmail(@RequestBody @Valid ResendVerifyEmailRequest resendVerifyEmailRequest, HttpServletRequest servletRequest) {
                authenticationService.resendVerifyEmail(resendVerifyEmailRequest);
                return ApiResponse.<Void>builder()
                        .message("Verification email has been resent. Please check your inbox")
                        .meta(buildMetaInfo(servletRequest))
                        .build();
        }

        @PostMapping("/login")
        ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest servletRequest) {
                ResponseEntity<AuthenticationResponse> responseEntity = authenticationService.login(loginRequest);
                ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                        .message("Login successful")
                        .data(responseEntity.getBody())
                        .meta(buildMetaInfo(servletRequest))
                        .build();

                return ResponseEntity.status(responseEntity.getStatusCode())
                        .headers(responseEntity.getHeaders())
                        .body(apiResponse);
        }

        @PostMapping("/logout")
        ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest servletRequest) {
                ResponseEntity<Void> responseEntity = authenticationService.logout();
                ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                        .message("Logout successful")
                        .meta(buildMetaInfo(servletRequest))
                        .build();

                return ResponseEntity.status(responseEntity.getStatusCode())
                        .headers(responseEntity.getHeaders())
                        .body(apiResponse);
        }

        @GetMapping("/account")
        ApiResponse<UserSecureResponse> getMyInfo(HttpServletRequest servletRequest) {
                return ApiResponse.<UserSecureResponse>builder()
                        .message("Authenticated user information retrieved successfully")
                        .data(authenticationService.getMyInfo())
                        .meta(buildMetaInfo(servletRequest))
                        .build();
        }

        @GetMapping("/refresh")
        ResponseEntity<ApiResponse<AuthenticationResponse>> getNewToken(@CookieValue(name = "refresh_token") String refreshToken, HttpServletRequest servletRequest) throws ParseException, JOSEException {
                ResponseEntity<AuthenticationResponse> responseEntity = authenticationService.getNewToken(refreshToken);
                ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                        .message("Refresh token and access token successfully retrieved")
                        .data(responseEntity.getBody())
                        .meta(buildMetaInfo(servletRequest))
                        .build();

                return ResponseEntity.status(responseEntity.getStatusCode())
                        .headers(responseEntity.getHeaders())
                        .body(apiResponse);
        }

        @PostMapping("/forgot-password")
        ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest servletRequest) {
                authenticationService.forgotPassword(forgotPasswordRequest);
                return ApiResponse.<Void>builder()
                        .message("Please check your email to reset your password")
                        .meta(buildMetaInfo(servletRequest))
                        .build();
        }

        @PostMapping("/reset-password")
        ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest, HttpServletRequest servletRequest) throws ParseException, JOSEException {
                authenticationService.resetPassword(resetPasswordRequest);
                return ApiResponse.<Void>builder()
                        .message("Your password has been successfully reset. Please login again")
                        .meta(buildMetaInfo(servletRequest))
                        .build();
        }
}