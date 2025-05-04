package com.bitorax.priziq.validation.annotation;

import com.bitorax.priziq.validation.validator.UserOrGuestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserOrGuestValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UserOrGuestValid {
    String message() default "USER_OR_GUEST_REQUIRED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}