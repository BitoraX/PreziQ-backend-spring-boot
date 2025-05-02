package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.dto.request.session.UpdateSessionRequest;
import com.bitorax.priziq.dto.response.session.SessionResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionResponse sessionToResponse(Session session);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSessionFromRequest(UpdateSessionRequest request, @MappingTarget Session session);
}
