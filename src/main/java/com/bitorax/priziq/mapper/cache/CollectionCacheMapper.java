package com.bitorax.priziq.mapper.cache;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.cache.CollectionCacheDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectionCacheMapper {
    @Mapping(source = "creator.userId", target = "creatorId")
    CollectionCacheDTO collectionToCacheDTO(Collection collection);

    @Mapping(source = "creatorId", target = "creator.userId")
    Collection collectionCacheDTOToCollection(CollectionCacheDTO collectionCacheDTO);
}
