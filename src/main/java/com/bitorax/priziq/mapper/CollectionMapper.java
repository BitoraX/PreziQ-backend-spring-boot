package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    CollectionResponse collectionToResponse(Collection collection);

    Collection createCollectionRequestToCollection(CreateCollectionRequest createCollectionRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCollectionRequestToCollection(@MappingTarget Collection collection, UpdateCollectionRequest updateCollectionRequest);

    List<CollectionResponse> collectionsToCollectionResponseList(List<Collection> collections);
}
