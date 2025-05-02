package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.session_participant.UpdateSessionParticipantRequest;
import com.bitorax.priziq.dto.response.session.SessionParticipantResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SessionParticipantMapper {
    SessionParticipantResponse sessionParticipantToResponse(SessionParticipant sessionParticipant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSessionParticipantFromRequest(UpdateSessionParticipantRequest updateSessionParticipantRequest, @MappingTarget SessionParticipant sessionParticipant);
}
