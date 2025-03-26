package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;
import com.bitorax.priziq.dto.response.common.PaginationMeta;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.CollectionMapper;
import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.service.CollectionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public CollectionResponse getCollectionById(String collectionId){
        return collectionMapper.collectionToResponse(collectionRepository.findById(collectionId).orElseThrow(() -> new AppException(ErrorCode.COLLECTION_NOT_FOUND)));
    }

    @Override
    public PaginationResponse getAllCollectionWithQuery(Specification<Collection> spec, Pageable pageable) {
        Page<Collection> collectionPage = this.collectionRepository.findAll(spec, pageable);
        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1) // base-index = 0
                        .pageSize(pageable.getPageSize())
                        .totalPages(collectionPage.getTotalPages())
                        .totalElements(collectionPage.getTotalElements())
                        .hasNext(collectionPage.hasNext())
                        .hasPrevious(collectionPage.hasPrevious())
                        .build())
                .content(this.collectionMapper.collectionsToCollectionResponseList(collectionPage.getContent()))
                .build();
    }

    @Override
    public CollectionResponse updateCollectionById(String collectionId, UpdateCollectionRequest updateCollectionRequest){
        Collection currentCollection = this.collectionRepository.findById(collectionId).orElseThrow(() -> new AppException(ErrorCode.COLLECTION_NOT_FOUND));
        this.collectionMapper.updateCollectionRequestToCollection(currentCollection, updateCollectionRequest);
        return this.collectionMapper.collectionToResponse(collectionRepository.save(currentCollection));
    }

    @Override
    public void deleteCollectionById(String collectionId){
        Collection currentCollection = this.collectionRepository.findById(collectionId).orElseThrow(() -> new AppException(ErrorCode.COLLECTION_NOT_FOUND));
        this.collectionRepository.delete(currentCollection);
    }
}
