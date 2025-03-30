package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.ActivityReorderRequest;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;
import com.bitorax.priziq.dto.response.collection.ReorderedActivityResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface CollectionService {
    CollectionResponse createCollection(CreateCollectionRequest createCollectionRequest);

    CollectionResponse getCollectionById(String collectionId);

    PaginationResponse getAllCollectionWithQuery(Specification<Collection> spec, Pageable pageable);

    CollectionResponse updateCollectionById(String collectionId, UpdateCollectionRequest updateCollectionRequest);

    void deleteCollectionById(String collectionId);

    List<ReorderedActivityResponse> reorderActivities(String collectionId, ActivityReorderRequest activityReorderRequest);
}
