package com.bitorax.priziq.mapper.cache;

import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.dto.cache.SessionCacheDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionCacheMapper {
    @Mapping(source = "collection.collectionId", target = "collectionId")
    @Mapping(source = "hostUser.userId", target = "hostUserId")
    SessionCacheDTO sessionToCacheDTO(Session session);

    @Mapping(source = "collectionId", target = "collection.collectionId")
    @Mapping(source = "hostUserId", target = "hostUser.userId")
    Session sessionCacheDTOToSession(SessionCacheDTO sessionCacheDTO);
}
