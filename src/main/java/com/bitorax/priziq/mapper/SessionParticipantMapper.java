package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.response.session.SessionParticipantDetailResponse;
import com.bitorax.priziq.dto.response.session.SessionParticipantSummaryResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionParticipantMapper {
    SessionParticipantSummaryResponse sessionParticipantToSummaryResponse(SessionParticipant sessionParticipant);

    SessionParticipantDetailResponse sessionParticipantToDetailResponse(SessionParticipant sessionParticipant);

    List<SessionParticipantSummaryResponse> sessionParticipantsToSummaryResponseList(List<SessionParticipant> sessionParticipants);

    List<SessionParticipantDetailResponse> sessionParticipantsToDetailResponseList(List<SessionParticipant> sessionParticipants);
}
