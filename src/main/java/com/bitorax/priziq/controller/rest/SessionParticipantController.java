package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.dto.request.session.session_participant.UpdateSessionParticipantRequest;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;
import com.bitorax.priziq.service.SessionParticipantService;
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
@RequestMapping("/api/v1/session-participants")
public class SessionParticipantController {
    SessionParticipantService sessionParticipantService;

    @PatchMapping("/{sessionParticipantId}")
    public ApiResponse<SessionParticipantResponse> updateSessionParticipantById(@PathVariable String sessionParticipantId, @RequestBody @Valid UpdateSessionParticipantRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.<SessionParticipantResponse>builder()
                .message("Session participant updated successfully")
                .data(sessionParticipantService.updateSessionParticipantById(sessionParticipantId, request))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}