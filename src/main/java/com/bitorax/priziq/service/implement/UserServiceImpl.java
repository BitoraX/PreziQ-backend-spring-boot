package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.RegionType;
import com.bitorax.priziq.domain.Role;
import com.bitorax.priziq.dto.request.auth.VerifyEmailRequest;
import com.bitorax.priziq.dto.request.user.*;
import com.bitorax.priziq.dto.response.common.PaginationMeta;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.user.UserResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.UserMapper;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.EmailService;
import com.bitorax.priziq.service.S3FileStorageService;
import com.bitorax.priziq.service.UserService;
import com.bitorax.priziq.utils.PhoneNumberUtils;
import com.bitorax.priziq.utils.RoleUtils;
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
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    EmailService emailService;
    S3FileStorageService s3FileStorageService;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SecurityUtils securityUtils;
    PhoneNumberUtils phoneNumberUtils;
    RoleUtils roleUtils;

    @Override
    public UserSecureResponse updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest) {
        User userAuthenticated = securityUtils.getAuthenticatedUser();
        userMapper.updateUserProfileRequestToUser(userAuthenticated, updateUserProfileRequest);
        return userMapper.userToSecureResponse(userRepository.save(userAuthenticated));
    }

    @Override
    public UserSecureResponse updateUserPassword(UpdateUserPasswordRequest updateUserPasswordRequest) {
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();

        if (!passwordEncoder.matches(updateUserPasswordRequest.getCurrentPassword(), userAuthenticated.getPassword()))
            throw new ApplicationException(ErrorCode.PASSWORD_MISMATCH);
        if (!updateUserPasswordRequest.getNewPassword().equals(updateUserPasswordRequest.getConfirmPassword()))
            throw new ApplicationException(ErrorCode.PASSWORD_AND_CONFIRM_MISMATCH);
        if (passwordEncoder.matches(updateUserPasswordRequest.getNewPassword(), userAuthenticated.getPassword()))
            throw new ApplicationException(ErrorCode.PASSWORD_SAME_AS_CURRENT);

        String hashPassword = this.passwordEncoder.encode(updateUserPasswordRequest.getNewPassword());
        userAuthenticated.setPassword(hashPassword);
        return this.userMapper.userToSecureResponse(this.userRepository.save(userAuthenticated));
    }

    @Override
    public void updateUserEmail(UpdateUserEmailRequest updateUserEmailRequest) {
        User userAuthenticated = securityUtils.getAuthenticatedUser();
        String newEmail = updateUserEmailRequest.getNewEmail();

        securityUtils.enforceProtectedEmailPolicy(userAuthenticated.getEmail()); // can't change system email
        if (userAuthenticated.getEmail().equals(newEmail))
            throw new ApplicationException(ErrorCode.NEW_EMAIL_SAME_BEFORE);
        if (userRepository.existsByEmail(newEmail))
            throw new ApplicationException(ErrorCode.EMAIL_EXISTED);

        // Set temporary new email to current user and send verify token to this email
        userAuthenticated.setEmail(newEmail);
        emailService.sendVerifyChangeEmail(userAuthenticated);
    }

    @Override
    public UserSecureResponse verifyEmailAndChangeNewEmail(VerifyEmailRequest verifyEmailRequest) throws ParseException, JOSEException {
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
                .meta(PaginationMeta.builder()
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
        User user = this.userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();

        // Check role (if ADMIN response UserResponse, else response UserSecureResponse)
        boolean isAdmin = this.securityUtils.isAdmin(userAuthenticated);
        return isAdmin ? this.userMapper.userToResponse(user) : this.userMapper.userToSecureResponse(user);
    }

    @Override
    public UserResponse updateUserForAdmin(String userId, UpdateUserForAdminRequest updateUserForAdminRequest) {
        User currentUser = userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        securityUtils.enforceProtectedEmailPolicy(currentUser.getEmail()); // can't change system email

        // Check phone number and email is valid
        String currentEmail = updateUserForAdminRequest.getEmail();
        if (currentEmail != null && !currentEmail.isEmpty()) {
            if (userRepository.existsByEmail(currentEmail))
                throw new ApplicationException(ErrorCode.EMAIL_EXISTED);
        }

        String currentPhoneNumber = updateUserForAdminRequest.getPhoneNumber();
        if (currentPhoneNumber != null && !currentPhoneNumber.isEmpty()) {
            String formattedPhoneNumber = phoneNumberUtils.formatPhoneNumberToE164(currentPhoneNumber,
                    RegionType.VIETNAM.getAlpha2Code());
            if (userRepository.existsByPhoneNumber(formattedPhoneNumber))
                throw new ApplicationException(ErrorCode.PHONE_NUMBER_EXISTED);
        }

        // Check avatar (delete old avatar) and status isVerified
        String currentAvatar = updateUserForAdminRequest.getAvatar();
        if (currentAvatar != null)
            s3FileStorageService.deleteOldSingleImageIfPresent(currentUser.getAvatar());

        Boolean isVerifiedAccount = updateUserForAdminRequest.getIsVerified();
        if (currentUser.getIsVerified().equals(isVerifiedAccount))
            throw new ApplicationException(ErrorCode.USER_SAME_IS_VERIFY);

        userMapper.updateUserForAdminRequestToUser(currentUser, updateUserForAdminRequest);

        // Get roleIds and map List<Role> to User entity
        List<String> roleIds = updateUserForAdminRequest.getRoleIds();

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<String> uniqueRoleIds = new HashSet<>(roleIds);
            if (uniqueRoleIds.size() < roleIds.size())
                throw new ApplicationException(ErrorCode.DUPLICATE_ROLE_IDS);

            List<Role> newRoles = roleUtils.validateRolesExist(roleIds);
            roleUtils.validateUserDoesNotAlreadyHaveRoles(currentUser, newRoles);

            currentUser.getRoles().addAll(newRoles);
        }

        return userMapper.userToResponse(userRepository.save(currentUser));
    }

    @Override
    public void deleteUserById(String userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        securityUtils.enforceProtectedEmailPolicy(currentUser.getEmail());

        // Delete user account if not system account
        currentUser.getRoles().clear(); // JPA create DELETE query (role_users)
        userRepository.delete(currentUser);
    }

    @Override
    public void deleteRoleFromUser(String userId, DeleteRoleFromUserRequest deleteRoleFromUserRequest) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        List<String> roleIds = deleteRoleFromUserRequest.getRoleIds();

        roleUtils.checkDuplicateRoleIds(roleIds);
        roleUtils.validateRolesExist(roleIds);

        Set<String> existingRoleIdsInUser = roleUtils.getRoleIdsFromUser(currentUser);
        Set<String> nonExistentInUser = roleIds.stream()
                .filter(id -> !existingRoleIdsInUser.contains(id))
                .collect(Collectors.toSet());

        if (!nonExistentInUser.isEmpty()) {
            throw new ApplicationException(ErrorCode.ROLE_NOT_IN_USER,
                    "Vai trò với ID: " + nonExistentInUser + " không có trong người dùng " + currentUser.getEmail());
        }

        currentUser.getRoles().removeIf(role -> roleIds.contains(role.getRoleId()));
        userRepository.save(currentUser);
    }
}
