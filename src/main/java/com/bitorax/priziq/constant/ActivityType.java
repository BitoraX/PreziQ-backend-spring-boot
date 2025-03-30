package com.bitorax.priziq.constant;

import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ActivityType {
    QUIZ_BUTTONS,
    QUIZ_CHECKBOXES,
    QUIZ_REORDER,
    QUIZ_TYPE_ANSWER,
    QUIZ_TRUE_OR_FALSE,
    INFO_SLIDE

    ;

    public static void validateActivityType(String type) {
        boolean isValid = Arrays.stream(values()).anyMatch(activityType -> activityType.name().equalsIgnoreCase(type));
        if (!isValid)
            throw new AppException(ErrorCode.INVALID_ACTIVITY_TYPE);
    }
}
