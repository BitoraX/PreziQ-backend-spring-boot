package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionDetailResponse;
import com.bitorax.priziq.dto.response.collection.CollectionSummaryResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    CollectionDetailResponse collectionToDetailResponse(Collection collection);

    CollectionSummaryResponse collectionToSummaryResponse(Collection collection);

    @Mapping(target = "activities", ignore = true)
    Collection createCollectionRequestToCollection(CreateCollectionRequest createCollectionRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "activities", ignore = true)
    void updateCollectionRequestToCollection(@MappingTarget Collection collection, UpdateCollectionRequest updateCollectionRequest);

    List<CollectionDetailResponse> collectionsToCollectionDetailResponseList(List<Collection> collections);

    List<CollectionSummaryResponse> collectionsToCollectionSummaryResponseList(List<Collection> collections);
}
