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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
        AuthenticationService authenticationService;

        @PostMapping("/register")
        ApiResponse<Void> register(@RequestBody @Valid RegisterUserRequest registerUserRequest,
                        HttpServletRequest servletRequest) {
                this.authenticationService.register(registerUserRequest);
                return ApiResponse.<Void>builder()
                                .statusCode(HttpStatus.NO_CONTENT.value())
                                .message("Vui lòng kiểm tra email để xác thực tài khoản của bạn")
                                .path(servletRequest.getRequestURI())
                                .build();
        }

        @PostMapping("/verify-active-account")
        ApiResponse<AuthenticationResponse> verifyEmailAndActivateAccount(
                        @RequestBody @Valid VerifyEmailRequest verifyEmailRequest, HttpServletRequest servletRequest)
                        throws ParseException, JOSEException {
                return ApiResponse.<AuthenticationResponse>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Email của bạn đã được xác thực thành công")
                                .data(this.authenticationService.verifyEmailAndActivateAccount(verifyEmailRequest))
                                .path(servletRequest.getRequestURI())
                                .build();
        }

        @PostMapping("/resend-verify")
        ApiResponse<Void> resendVerifyEmail(@RequestBody @Valid ResendVerifyEmailRequest resendVerifyEmailRequest,
                        HttpServletRequest servletRequest) {
                this.authenticationService.resendVerifyEmail(resendVerifyEmailRequest);
                return ApiResponse.<Void>builder()
                                .statusCode(HttpStatus.NO_CONTENT.value())
                                .message("Chúng tôi đã gửi lại email. Hãy kiểm tra email để xác thực tài khoản của bạn")
                                .path(servletRequest.getRequestURI())
                                .build();
        }

        @PostMapping("/login")
        ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                        HttpServletRequest servletRequest) {
                ResponseEntity<AuthenticationResponse> responseEntity = authenticationService.login(loginRequest);
                ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Đăng nhập thành công")
                                .data(responseEntity.getBody())
                                .path(servletRequest.getRequestURI())
                                .build();

                return ResponseEntity.status(responseEntity.getStatusCode())
                                .headers(responseEntity.getHeaders())
                                .body(apiResponse);
        }

        @PostMapping("/logout")
        ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest servletRequest) {
                ResponseEntity<Void> responseEntity = authenticationService.logout();
                ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .statusCode(HttpStatus.NO_CONTENT.value())
                                .message("Đăng xuất thành công")
                                .path(servletRequest.getRequestURI())
                                .build();

                return ResponseEntity.status(responseEntity.getStatusCode())
                                .headers(responseEntity.getHeaders())
                                .body(apiResponse);
        }

        @GetMapping("/account")
        ApiResponse<UserSecureResponse> getMyInfo(HttpServletRequest servletRequest) {
                return ApiResponse.<UserSecureResponse>builder()
                                .statusCode(HttpStatus.OK.value())
                                .message("Lấy thông tin người dùng đang đăng nhập thành công")
                                .data(this.authenticationService.getMyInfo())
                                .path(servletRequest.getRequestURI())
                                .build();
        }

        @GetMapping("/refresh")
        ResponseEntity<ApiResponse<AuthenticationResponse>> getNewToken(
                        @CookieValue(name = "refresh_token") String refreshToken, HttpServletRequest servletRequest)
                        throws ParseException, JOSEException {
                ResponseEntity<AuthenticationResponse> responseEntity = authenticationService.getNewToken(refreshToken);
                ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                                .statusCode(HttpStatus.NO_CONTENT.value())
                                .message("Lấy bộ refresh/access token thành công")
                                .data(responseEntity.getBody())
                                .path(servletRequest.getRequestURI())
                                .build();

                return ResponseEntity.status(responseEntity.getStatusCode())
                                .headers(responseEntity.getHeaders())
                                .body(apiResponse);
        }

        @PostMapping("/forgot-password")
        ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest,
                        HttpServletRequest servletRequest) {
                this.authenticationService.forgotPassword(forgotPasswordRequest);
                return ApiResponse.<Void>builder()
                                .statusCode(HttpStatus.NO_CONTENT.value())
                                .message("Vui lòng kiểm tra email để lấy lại mật khẩu tài khoản của bạn")
                                .path(servletRequest.getRequestURI())
                                .build();
        }

        @PostMapping("/reset-password")
        ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest,
                        HttpServletRequest servletRequest) throws ParseException, JOSEException {
                this.authenticationService.resetPassword(resetPasswordRequest);
                return ApiResponse.<Void>builder()
                                .statusCode(HttpStatus.NO_CONTENT.value())
                                .message("Tài khoản của bạn đã đổi mật khẩu thành công. Vui lòng đăng nhập lại")
                                .path(servletRequest.getRequestURI())
                                .build();
        }
}
