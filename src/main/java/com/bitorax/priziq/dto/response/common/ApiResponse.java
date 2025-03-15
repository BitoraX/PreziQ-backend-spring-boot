package com.bitorax.priziq.dto.response.common;

import com.bitorax.priziq.exception.ErrorDetail;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // if field equal null, don't appear in body response
public class ApiResponse<T> {
    @Builder.Default
    int code = 1000;

    int statusCode;
    String message;

    T data; // success request
    List<ErrorDetail> errors; // fail request

    @Builder.Default
    Instant timestamp = Instant.now();

    String path;
}
