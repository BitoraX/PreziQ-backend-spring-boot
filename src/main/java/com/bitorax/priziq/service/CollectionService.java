package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.ActivityReorderRequest;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionDetailResponse;
import com.bitorax.priziq.dto.response.collection.ReorderedActivityResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface CollectionService {
    CollectionDetailResponse createCollection(CreateCollectionRequest createCollectionRequest);

    CollectionDetailResponse getCollectionById(String collectionId);

    PaginationResponse getMyCollections(Specification<Collection> spec, Pageable pageable);

    PaginationResponse getAllCollectionWithQuery(Specification<Collection> spec, Pageable pageable);

    CollectionDetailResponse updateCollectionById(String collectionId, UpdateCollectionRequest updateCollectionRequest);

    void deleteCollectionById(String collectionId);

    List<ReorderedActivityResponse> reorderActivities(String collectionId, ActivityReorderRequest activityReorderRequest);
}
