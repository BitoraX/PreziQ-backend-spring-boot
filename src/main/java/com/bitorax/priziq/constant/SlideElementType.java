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
public enum SlideElementType {
    TEXT,
    IMAGE,

    ;

    public static void validateSlideElementType(String type) {
        boolean isValid = Arrays.stream(values()).anyMatch(slideElementType -> slideElementType.name().equalsIgnoreCase(type));
        if (!isValid) {
            throw new ApplicationException(ErrorCode.INVALID_SLIDE_ELEMENT_TYPE);
        }
    }
}
