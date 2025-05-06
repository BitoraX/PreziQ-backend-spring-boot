package com.bitorax.priziq.exception;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.stream.Collectors;

public class ErrorDetailMapper {

    public static List<ErrorDetail> mapValidationErrors(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    ErrorCode errorCode;
                    try {
                        errorCode = ErrorCode.valueOf(error.getDefaultMessage());
                    } catch (IllegalArgumentException e) {
                        errorCode = ErrorCode.INVALID_KEY;
                    }

                    String resource = null;
                    String field = null;
                    if (error instanceof FieldError fieldError) {
                        resource = fieldError.getObjectName();
                        field = fieldError.getField();
                    }

                    return ErrorDetail.builder()
                            .resource(resource)
                            .field(field)
                            .code(errorCode.getCode())
                            .message(errorCode.getMessage())
                            .build();
                })
                .collect(Collectors.toList());
    }
}