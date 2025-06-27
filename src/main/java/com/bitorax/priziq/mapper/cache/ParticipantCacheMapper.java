package com.bitorax.priziq.mapper.cache;

import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.cache.ParticipantCacheDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParticipantCacheMapper {
    @Mapping(source = "session.sessionId", target = "sessionId")
    @Mapping(source = "user.userId", target = "userId")
    ParticipantCacheDTO sessionParticipantToCacheDTO(SessionParticipant sessionParticipant);

    @Mapping(source = "sessionId", target = "session.sessionId")
    @Mapping(source = "userId", target = "user.userId")
    SessionParticipant participantCacheDTOToSessionParticipant(ParticipantCacheDTO participantCacheDTO);
}
