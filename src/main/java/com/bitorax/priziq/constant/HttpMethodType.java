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
public enum HttpMethodType {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS;

    public static void validateHttpMethod(String method) {
        boolean isValid = Arrays.stream(values()).anyMatch(httpMethod -> httpMethod.name().equalsIgnoreCase(method));
        if (!isValid)
            throw new ApplicationException(ErrorCode.INVALID_HTTP_METHOD);
    }
}
