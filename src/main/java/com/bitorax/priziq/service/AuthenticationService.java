package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.auth.*;
import com.bitorax.priziq.dto.response.auth.AuthenticationResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;

public interface AuthenticationService {
    void register(RegisterUserRequest registerUserRequest);

    AuthenticationResponse verifyEmailAndActivateAccount(VerifyEmailRequest verifyEmailRequest) throws ParseException, JOSEException;

    ResponseEntity<AuthenticationResponse> login(LoginRequest loginRequest);

    ResponseEntity<Void> logout();

    UserSecureResponse getMyInfo();

    ResponseEntity<AuthenticationResponse> getNewToken(String refreshToken) throws ParseException, JOSEException;

    void resendVerifyEmail(ResendVerifyEmailRequest resendVerifyEmailRequest);

    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest) throws ParseException, JOSEException;
}
