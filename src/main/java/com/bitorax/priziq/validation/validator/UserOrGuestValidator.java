package com.bitorax.priziq.validation.validator;

import com.bitorax.priziq.dto.request.session.session_participant.JoinSessionRequest;
import com.bitorax.priziq.validation.annotation.UserOrGuestValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserOrGuestValidator implements ConstraintValidator<UserOrGuestValid, JoinSessionRequest> {
    @Override
    public boolean isValid(JoinSessionRequest request, ConstraintValidatorContext context) {
        return (request.getUserId() != null && !request.getUserId().trim().isEmpty())
                || (request.getGuestName() != null && !request.getGuestName().trim().isEmpty());
    }
}
