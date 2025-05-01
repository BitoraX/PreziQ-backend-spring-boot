package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.dto.request.session.session_participant.CreateSessionParticipantRequest;
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

    @PostMapping
    public ApiResponse<SessionParticipantResponse> createSessionParticipant(@RequestBody @Valid CreateSessionParticipantRequest createSessionParticipantRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<SessionParticipantResponse>builder()
                .message("Session participant created successfully")
                .data(sessionParticipantService.createSessionParticipant(createSessionParticipantRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<SessionParticipantResponse> updateSessionParticipantById(@PathVariable("id") String sessionParticipantId, @RequestBody @Valid UpdateSessionParticipantRequest updateSessionParticipantRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<SessionParticipantResponse>builder()
                .message("Session participant updated successfully")
                .data(sessionParticipantService.updateSessionParticipantById(sessionParticipantId, updateSessionParticipantRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}