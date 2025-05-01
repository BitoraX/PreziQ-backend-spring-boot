package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.dto.response.session.SessionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionResponse sessionToResponse(Session session);
}
