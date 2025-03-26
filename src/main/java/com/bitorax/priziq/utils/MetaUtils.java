package com.bitorax.priziq.utils;

import com.bitorax.priziq.dto.response.common.MetaInfo;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

public class MetaUtils {

    private MetaUtils() {
        // private constructor to prevent instantiation
    }

    public static MetaInfo buildMetaInfo(HttpServletRequest request) {
        return MetaInfo.builder()
                .timestamp(Instant.now().toString())
                .instance(request.getRequestURI())
                .build();
    }
}