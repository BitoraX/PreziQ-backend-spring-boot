package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.session.SessionHistoryResponse;
import com.bitorax.priziq.dto.response.session.SessionDetailResponse;
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
    public ApiResponse<SessionDetailResponse> createSession(@RequestBody @Valid CreateSessionRequest createSessionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<SessionDetailResponse>builder()
                .message("Session created successfully")
                .data(sessionService.createSession(createSessionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{sessionId}/history")
    ApiResponse<SessionHistoryResponse> getSessionHistory(@PathVariable("sessionId") String sessionId, HttpServletRequest servletRequest) {
        return ApiResponse.<SessionHistoryResponse>builder()
                .message("Session history retrieved successfully")
                .data(sessionService.getSessionHistory(sessionId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
