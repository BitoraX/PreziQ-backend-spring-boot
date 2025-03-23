package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;
import com.bitorax.priziq.mapper.CollectionMapper;
import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.service.CollectionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollectionServiceImp implements CollectionService {
    CollectionRepository collectionRepository;
    CollectionMapper collectionMapper;

    @Override
    public CollectionResponse createCollection(CreateCollectionRequest createCollectionRequest){
        Collection collection = collectionMapper.createCollectionRequestToCollection(createCollectionRequest);
        return collectionMapper.collectionToResponse(collectionRepository.save(collection));
    }
}
