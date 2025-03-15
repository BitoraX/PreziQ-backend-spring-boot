package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.RegionType;
import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.dto.request.auth.VerifyEmailRequest;
import com.bitorax.priziq.dto.request.user.*;
import com.bitorax.priziq.dto.response.common.MetaResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.user.UserResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.UserMapper;
import com.bitorax.priziq.repository.RoleRepository;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.EmailService;
import com.bitorax.priziq.service.S3FileStorageService;
import com.bitorax.priziq.service.UserService;
import com.bitorax.priziq.utils.PhoneNumberUtils;
import com.bitorax.priziq.utils.SecurityUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImp implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    EmailService emailService;
    S3FileStorageService s3FileStorageService;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SecurityUtils securityUtils;
    PhoneNumberUtils phoneNumberUtils;

    @Override
    public UserSecureResponse updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest) {
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();

        // String currentAvatarUrl = userAuthenticated.getAvatar();
        // String updateAvatarUrl = updateUserProfileRequest.getAvatar();
        // if (updateAvatarUrl != null && !Objects.equals(currentAvatarUrl,
        // updateAvatarUrl)) {
        // this.s3FileStorageService.deleteOldSingleImageIfPresent(currentAvatarUrl);
        // }

        this.userMapper.updateUserProfileRequestToUser(userAuthenticated, updateUserProfileRequest);
        return this.userMapper.userToSecureResponse(this.userRepository.save(userAuthenticated));
    }

    @Override
    public UserSecureResponse updateUserPassword(UpdateUserPasswordRequest updateUserPasswordRequest) {
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();

        if (!passwordEncoder.matches(updateUserPasswordRequest.getCurrentPassword(), userAuthenticated.getPassword()))
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        if (!updateUserPasswordRequest.getNewPassword().equals(updateUserPasswordRequest.getConfirmPassword()))
            throw new AppException(ErrorCode.PASSWORD_AND_CONFIRM_MISMATCH);
        if (passwordEncoder.matches(updateUserPasswordRequest.getNewPassword(), userAuthenticated.getPassword()))
            throw new AppException(ErrorCode.PASSWORD_SAME_AS_CURRENT);

        String hashPassword = this.passwordEncoder.encode(updateUserPasswordRequest.getNewPassword());
        userAuthenticated.setPassword(hashPassword);
        return this.userMapper.userToSecureResponse(this.userRepository.save(userAuthenticated));
    }

    @Override
    public void updateUserEmail(UpdateUserEmailRequest updateUserEmailRequest) {
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();
        String newEmail = updateUserEmailRequest.getNewEmail();

        this.securityUtils.enforceProtectedEmailPolicy(userAuthenticated.getEmail()); // can't change system email
        if (userAuthenticated.getEmail().equals(newEmail))
            throw new AppException(ErrorCode.NEW_EMAIL_SAME_BEFORE);
        if (this.userRepository.existsByEmail(newEmail))
            throw new AppException(ErrorCode.EMAIL_EXISTED);

        // Set temporary new email to current user and send verify token to this email
        userAuthenticated.setEmail(newEmail);
        this.emailService.sendVerifyEmail(userAuthenticated);
    }

    @Override
    public UserSecureResponse verifyEmailAndChangeNewEmail(VerifyEmailRequest verifyEmailRequest)
            throws ParseException, JOSEException {
        SignedJWT verifiedToken = this.securityUtils.verifyAccessToken(verifyEmailRequest.getToken());
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();

        String updateEmail = verifiedToken.getJWTClaimsSet().getStringClaim("email");
        userAuthenticated.setEmail(updateEmail);

        return userMapper.userToSecureResponse(this.userRepository.save(userAuthenticated));
    }

    @Override
    public PaginationResponse getAllUserWithQuery(Specification<User> spec, Pageable pageable) {
        Page<User> userPage = userRepository.findAll(spec, pageable);
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();

        return PaginationResponse.builder()
                .meta(MetaResponse.builder()
                        .currentPage(pageable.getPageNumber() + 1) // base-index = 0
                        .pageSize(pageable.getPageSize())
                        .totalPages(userPage.getTotalPages())
                        .totalElements(userPage.getTotalElements())
                        .hasNext(userPage.hasNext())
                        .hasPrevious(userPage.hasPrevious())
                        .build())
                .content(this.securityUtils.isAdmin(userAuthenticated)
                        ? this.userMapper.usersToUserResponseList(userPage.getContent())
                        : this.userMapper.usersToUserSecureResponseList(userPage.getContent()))
                .build();
    }

    @Override
    public Object getUserById(String userId) {
        User user = this.userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();

        // Check role (if ADMIN response UserResponse, else response UserSecureResponse)
        boolean isAdmin = this.securityUtils.isAdmin(userAuthenticated);
        return isAdmin ? this.userMapper.userToResponse(user) : this.userMapper.userToSecureResponse(user);
    }

    @Override
    public UserResponse updateUserForAdmin(String userId, UpdateUserForAdminRequest updateUserForAdminRequest) {
        User currentUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        this.securityUtils.enforceProtectedEmailPolicy(currentUser.getEmail()); // can't change system email

        // Check phone number and email is valid
        String currentEmail = updateUserForAdminRequest.getEmail();
        if (currentEmail != null && !currentEmail.isEmpty()) {
            if (this.userRepository.existsByEmail(currentEmail))
                throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        String currentPhoneNumber = updateUserForAdminRequest.getPhoneNumber();
        if (currentPhoneNumber != null && !currentPhoneNumber.isEmpty()) {
            String formattedPhoneNumber = this.phoneNumberUtils.formatPhoneNumberToE164(currentPhoneNumber,
                    RegionType.VIETNAM.getAlpha2Code());
            if (this.userRepository.existsByPhoneNumber(formattedPhoneNumber))
                throw new AppException(ErrorCode.PHONE_NUMBER_EXISTED);
        }

        // Check avatar (delete old avatar) and status isVerified
        String currentAvatar = updateUserForAdminRequest.getAvatar();
        if (currentAvatar != null)
            this.s3FileStorageService.deleteOldSingleImageIfPresent(currentUser.getAvatar());

        Boolean isVerifiedAccount = updateUserForAdminRequest.getIsVerified();
        if (currentUser.getIsVerified().equals(isVerifiedAccount))
            throw new AppException(ErrorCode.USER_SAME_IS_VERIFY);

        this.userMapper.updateUserForAdminRequestToUser(currentUser, updateUserForAdminRequest);

        // Get roleIds and map List<Role> to User entity
        List<String> roleIds = updateUserForAdminRequest.getRoleIds();

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<String> uniqueRoleIds = new HashSet<>(roleIds);
            if (uniqueRoleIds.size() < roleIds.size())
                throw new AppException(ErrorCode.DUPLICATE_ROLE_IDS);

            List<Role> newRoles = this.validateRolesExist(roleIds);
            this.validateUserDoesNotAlreadyHaveRoles(currentUser, newRoles);

            currentUser.getRoles().addAll(newRoles);
        }

        return this.userMapper.userToResponse(this.userRepository.save(currentUser));
    }

    @Override
    public void deleteUserById(String userId) {
        User currentUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        this.securityUtils.enforceProtectedEmailPolicy(currentUser.getEmail());

        // Delete user account if not system account
        currentUser.getRoles().clear(); // JPA create DELETE query (role_users)
        this.userRepository.delete(currentUser);
    }

    private void validateUserDoesNotAlreadyHaveRoles(User user, List<Role> newRoles) {
        Set<String> currentRoleIds = user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<String> duplicateRoles = newRoles.stream()
                .map(Role::getId)
                .filter(currentRoleIds::contains)
                .collect(Collectors.toSet());

        if (!duplicateRoles.isEmpty()) {
            String errorMessage = "Người dùng đã có vai trò với ID: " + duplicateRoles;
            throw new AppException(ErrorCode.ROLE_ALREADY_ASSIGNED, errorMessage);
        }
    }

    private List<Role> validateRolesExist(List<String> providedIds) {
        List<Role> existingRoles = this.roleRepository.findAllById(providedIds);

        Set<String> existingIds = existingRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<String> nonExistentIds = providedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toSet());

        if (!nonExistentIds.isEmpty()) {
            String customErrorMessage = "Vai trò với ID: " + nonExistentIds + " không tồn tại trên hệ thống";
            throw new AppException(ErrorCode.ROLE_NOT_FOUND, customErrorMessage);
        }

        return existingRoles;
    }
}
