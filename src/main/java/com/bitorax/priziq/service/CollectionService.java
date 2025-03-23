package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;

public interface CollectionService {
    CollectionResponse createCollection(CreateCollectionRequest createCollectionRequest);
}
