package com.bitorax.priziq.service;

import com.bitorax.priziq.constant.CollectionTopicType;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.ActivityReorderRequest;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionDetailResponse;
import com.bitorax.priziq.dto.response.collection.CollectionSummaryResponse;
import com.bitorax.priziq.dto.response.collection.ReorderedActivityResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface CollectionService {
    CollectionSummaryResponse createCollection(CreateCollectionRequest createCollectionRequest);

    CollectionDetailResponse getCollectionById(String collectionId);

    PaginationResponse getMyCollections(Specification<Collection> spec, Pageable pageable);

    PaginationResponse getAllCollectionWithQuery(Specification<Collection> spec, Pageable pageable);

    CollectionSummaryResponse updateCollectionById(String collectionId, UpdateCollectionRequest updateCollectionRequest);

    void deleteCollectionById(String collectionId);

    List<ReorderedActivityResponse> reorderActivities(String collectionId, ActivityReorderRequest activityReorderRequest);

    Map<String, List<CollectionSummaryResponse>> getCollectionsGroupedByTopic(CollectionTopicType topic, Pageable pageable);
}
