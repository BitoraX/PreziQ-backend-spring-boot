package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.dto.response.session.SessionDetailResponse;
import com.bitorax.priziq.dto.response.session.SessionSummaryResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDetailResponse sessionToDetailResponse(Session session);

    SessionSummaryResponse sessionToSummaryResponse(Session session);

    List<SessionDetailResponse> sessionsToDetailResponseList(List<Session> sessions);

    List<SessionSummaryResponse> sessionsToSummaryResponseList(List<Session> sessions);
}
