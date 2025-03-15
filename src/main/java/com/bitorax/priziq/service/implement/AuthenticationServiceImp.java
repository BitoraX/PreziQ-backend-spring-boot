package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.RegionType;
import com.bitorax.priziq.constant.RoleType;
import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.dto.request.auth.*;
import com.bitorax.priziq.dto.response.auth.AuthenticationResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.UserMapper;
import com.bitorax.priziq.repository.RoleRepository;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.EmailService;
import com.bitorax.priziq.utils.PhoneNumberUtils;
import com.bitorax.priziq.utils.SecurityUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bitorax.priziq.service.AuthenticationService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImp implements AuthenticationService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    EmailService emailService;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SecurityUtils securityUtils;
    PhoneNumberUtils phoneNumberUtils;

    @Override
    public void register(RegisterUserRequest registerUserRequest) {
        if (this.userRepository.existsByEmail(registerUserRequest.getEmail()))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        String formattedPhoneNumber = this.phoneNumberUtils
                .formatPhoneNumberToE164(registerUserRequest.getPhoneNumber(), RegionType.VIETNAM.getAlpha2Code());
        if (this.userRepository.existsByPhoneNumber(formattedPhoneNumber))
            throw new AppException(ErrorCode.PHONE_NUMBER_EXISTED);

        User user = this.userMapper.registerRequestToUser(registerUserRequest);
        user.setPassword(this.passwordEncoder.encode(registerUserRequest.getPassword()));
        user.setPhoneNumber(formattedPhoneNumber);

        // Set default USER role
        List<Role> roles = new ArrayList<>();
        roles.add(this.roleRepository.findByName(RoleType.USER_ROLE.getName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NAME_NOT_FOUND)));
        user.setRoles(roles);

        this.userRepository.save(user);
        this.emailService.sendVerifyEmail(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User currentUser = this.userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!currentUser.getIsVerified())
            throw new AppException(ErrorCode.NOT_VERIFIED_ACCOUNT);
        this.emailService.sendForgotPasswordEmail(currentUser);
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) throws ParseException, JOSEException {
        // Verify token and get current user by email
        SignedJWT verifiedToken = this.securityUtils.verifyAccessToken(resetPasswordRequest.getToken());
        String userId = verifiedToken.getJWTClaimsSet().getSubject();
        User currentUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // New password same current password
        if (passwordEncoder.matches(resetPasswordRequest.getNewPassword(), currentUser.getPassword()))
            throw new AppException(ErrorCode.PASSWORD_SAME_AS_CURRENT);

        // Update new password, logout user and notification login again
        currentUser.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        this.userRepository.save(currentUser);
    }

    @Override
    public AuthenticationResponse verifyEmailAndActivateAccount(VerifyEmailRequest verifyEmailRequest)
            throws ParseException, JOSEException {
        SignedJWT verifiedToken = this.securityUtils.verifyAccessToken(verifyEmailRequest.getToken());

        // Get information user and update status isVerified = true
        String userId = verifiedToken.getJWTClaimsSet().getSubject();

        User currentUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (currentUser.getIsVerified())
            throw new AppException(ErrorCode.NOT_VERIFIED_ACCOUNT_TWICE);

        currentUser.setIsVerified(true);
        userRepository.save(currentUser);

        return AuthenticationResponse.builder()
                .accessToken(this.securityUtils.generateAccessToken(currentUser))
                .userSecured(userMapper.userToSecureResponse(currentUser))
                .build();
    }

    @Override
    public void resendVerifyEmail(ResendVerifyEmailRequest resendVerifyEmailRequest) {
        User currentUser = this.userRepository.findByEmail(resendVerifyEmailRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (currentUser.getIsVerified())
            throw new AppException(ErrorCode.NOT_VERIFIED_ACCOUNT_TWICE);
        this.emailService.sendVerifyEmail(currentUser);
    }

    @Override
    public ResponseEntity<AuthenticationResponse> login(LoginRequest loginRequest) {
        User currentUser;

        String emailLoginForm = loginRequest.getEmail();
        String phoneNumberLoginForm = loginRequest.getPhoneNumber();
        String passwordLoginForm = loginRequest.getPassword();

        if (emailLoginForm != null && !emailLoginForm.isEmpty()) {
            currentUser = this.userRepository.findByEmail(emailLoginForm)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else {
            String formattedPhoneNumber = this.phoneNumberUtils.formatPhoneNumberToE164(phoneNumberLoginForm,
                    RegionType.VIETNAM.getAlpha2Code());
            currentUser = this.userRepository.findByPhoneNumber(formattedPhoneNumber)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        // Not verified account
        if (!currentUser.getIsVerified())
            throw new AppException(ErrorCode.NOT_VERIFIED_ACCOUNT);

        // Compare form request password with database password
        boolean isPasswordMatch = passwordEncoder.matches(passwordLoginForm, currentUser.getPassword());
        if (!isPasswordMatch)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return this.securityUtils.createAuthResponse(currentUser);
    }

    @Override
    public UserSecureResponse getMyInfo() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return this.userMapper.userToSecureResponse(
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    @Override
    public ResponseEntity<Void> logout() {
        // Update refresh token is null in user entity
        UserSecureResponse currentUser = this.getMyInfo();
        this.securityUtils.updateUserRefreshToken(null, currentUser.getEmail());

        // Remove refresh token in cookies
        ResponseCookie deleteSpringCookie = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .build();
    }

    @Override
    public ResponseEntity<AuthenticationResponse> getNewToken(String refreshToken)
            throws ParseException, JOSEException {
        // Validate refresh token (not expired, valid, same in database)
        SignedJWT verifiedRefreshToken = this.securityUtils.verifyRefreshToken(refreshToken);

        String userId = verifiedRefreshToken.getJWTClaimsSet().getSubject();
        User currentUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return this.securityUtils.createAuthResponse(currentUser);
    }
}
