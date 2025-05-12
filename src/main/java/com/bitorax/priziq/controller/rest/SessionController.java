package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.CreateSessionRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.session.SessionDetailResponse;
import com.bitorax.priziq.service.SessionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @GetMapping("/me")
    ApiResponse<PaginationResponse> getMySessions(@Filter Specification<Session> spec, Pageable pageable, HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("My sessions retrieved successfully")
                .data(sessionService.getMySessions(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{sessionId}/participants")
    ApiResponse<PaginationResponse> getAllParticipantHistoryWithQuery(
            @PathVariable String sessionId,
            @Filter Specification<SessionParticipant> spec,
            Pageable pageable,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Session participants retrieved successfully with query filters")
                .data(sessionService.getAllParticipantHistoryWithQuery(sessionId, spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{sessionId}/participants/{participantId}/submissions")
    ApiResponse<PaginationResponse> getAllActivitySubmissionHistoryWithQuery(
            @PathVariable String sessionId,
            @PathVariable String participantId,
            @Filter Specification<ActivitySubmission> spec,
            Pageable pageable,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Activity submissions retrieved successfully with query filters")
                .data(sessionService.getAllActivitySubmissionHistoryWithQuery(sessionId,participantId, spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
