package com.bitorax.priziq.constant;

import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum SessionStatus {
    PENDING,
    STARTED,
    ENDED

    ;

    public static void validateSessionStatus(String status) {
        boolean isValid = Arrays.stream(values()).anyMatch(sessionStatus -> sessionStatus.name().equalsIgnoreCase(status));
        if (!isValid) {
            throw new ApplicationException(ErrorCode.INVALID_SESSION_STATUS);
        }
    }
}
