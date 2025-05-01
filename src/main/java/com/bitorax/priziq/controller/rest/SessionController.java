package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.request.session.UpdateSessionRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.session.SessionResponse;
import com.bitorax.priziq.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/sessions")
public class SessionController {
    SessionService sessionService;

    @PostMapping
    public ApiResponse<SessionResponse> createSession(@RequestBody @Valid CreateSessionRequest createSessionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<SessionResponse>builder()
                .message("Session created successfully")
                .data(sessionService.createSession(createSessionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{sessionId}")
    ApiResponse<SessionResponse> updateSessionById(@PathVariable String sessionId, @RequestBody @Valid UpdateSessionRequest updateSessionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<SessionResponse>builder()
                .message("Session information updated successfully")
                .data(sessionService.updateSessionById(sessionId, updateSessionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
