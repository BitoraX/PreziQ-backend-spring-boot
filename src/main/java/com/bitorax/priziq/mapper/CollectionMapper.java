package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    CollectionResponse collectionToResponse(Collection collection);

    Collection createCollectionRequestToCollection(CreateCollectionRequest createCollectionRequest);
}
