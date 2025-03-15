package com.bitorax.priziq.validation.validator;

import com.bitorax.priziq.validation.annotation.LoginIdentifierValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.bitorax.priziq.dto.request.auth.LoginRequest;

public class LoginIdentifierValidator implements ConstraintValidator<LoginIdentifierValid, LoginRequest> {

    @Override
    public boolean isValid(LoginRequest loginRequest, ConstraintValidatorContext context) {
        String email = loginRequest.getEmail();
        String phoneNumber = loginRequest.getPhoneNumber();

        if ((email == null || email.isEmpty()) && (phoneNumber == null || phoneNumber.isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("EMAIL_OR_PHONE_REQUIRED").addConstraintViolation();
            return false;
        }

        if (email != null && !email.isEmpty() && phoneNumber != null && !phoneNumber.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("ONLY_EMAIL_OR_PHONE").addConstraintViolation();
            return false;
        }

        return true;
    }
}
