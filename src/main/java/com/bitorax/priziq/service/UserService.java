package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.dto.request.auth.VerifyEmailRequest;
import com.bitorax.priziq.dto.request.user.*;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.user.UserResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.nimbusds.jose.JOSEException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;

public interface UserService {
    UserSecureResponse updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest);

    UserSecureResponse updateUserPassword(UpdateUserPasswordRequest updateUserPasswordRequest);

    UserSecureResponse verifyEmailAndChangeNewEmail(VerifyEmailRequest verifyEmailRequest) throws ParseException, JOSEException;

    void updateUserEmail(UpdateUserEmailRequest updateUserEmailRequest);

    PaginationResponse getAllUserWithQuery(Specification<User> spec, Pageable pageable);

    UserSecureResponse getUserById(String userId);

    UserResponse updateUserForAdmin(String userId, UpdateUserForAdminRequest updateUserForAdminRequest);

    void deleteRoleFromUser(String userId, DeleteRoleFromUserRequest deleteRoleFromUserRequest);

    void deleteUserById(String userId);
}
