package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.dto.request.collection.ActivityReorderRequest;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionDetailResponse;
import com.bitorax.priziq.dto.response.collection.ReorderedActivityResponse;
import com.bitorax.priziq.dto.response.common.PaginationMeta;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.CollectionMapper;
import com.bitorax.priziq.repository.ActivityRepository;
import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.CollectionService;
import com.bitorax.priziq.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollectionServiceImpl implements CollectionService {
    CollectionRepository collectionRepository;
    ActivityRepository activityRepository;
    UserRepository userRepository;
    CollectionMapper collectionMapper;

    @Override
    public CollectionDetailResponse createCollection(CreateCollectionRequest createCollectionRequest){
        Collection collection = collectionMapper.createCollectionRequestToCollection(createCollectionRequest);

        User creator = this.userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt()).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        collection.setCreator(creator);

        return collectionMapper.collectionToDetailResponse(collectionRepository.save(collection));
    }

    @Override
    public CollectionDetailResponse getCollectionById(String collectionId){
        return collectionMapper.collectionToDetailResponse(collectionRepository.findById(collectionId).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND)));
    }

    @Override
    public PaginationResponse getMyCollections(Specification<Collection> spec, Pageable pageable) {
        User creator = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        // Filter by creator and merge with client Specification if present
        Specification<Collection> creatorSpec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("creator").get("userId"), creator.getUserId());

        Specification<Collection> finalSpec = spec != null ? Specification.where(spec).and(creatorSpec) : creatorSpec;

        return getAllCollectionWithQuery(finalSpec, pageable);
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
                .content(this.collectionMapper.collectionsToCollectionDetailResponseList(collectionPage.getContent()))
                .build();
    }

    @Override
    public CollectionDetailResponse updateCollectionById(String collectionId, UpdateCollectionRequest updateCollectionRequest){
        Collection currentCollection = this.collectionRepository.findById(collectionId).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));
        this.collectionMapper.updateCollectionRequestToCollection(currentCollection, updateCollectionRequest);
        return this.collectionMapper.collectionToDetailResponse(collectionRepository.save(currentCollection));
    }

    @Override
    public void deleteCollectionById(String collectionId){
        Collection currentCollection = this.collectionRepository.findById(collectionId).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));
        this.collectionRepository.delete(currentCollection);
    }

    @Override
    @Transactional
    public List<ReorderedActivityResponse> reorderActivities(String collectionId, ActivityReorderRequest activityReorderRequest) {
        // Get collection or throw if not found
        Collection currentCollection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND, "Collection ID: " + collectionId + " not found"));

        // Get all activity IDs in this collection
        Set<String> currentActivityIds = currentCollection.getActivities().stream()
                .map(Activity::getActivityId)
                .collect(Collectors.toSet());

        List<String> newOrderList = activityReorderRequest.getOrderedActivityIds();

        // Validate all IDs belong to this collection
        for (String activityId : newOrderList) {
            if (!currentActivityIds.contains(activityId)) {
                throw new ApplicationException(
                        ErrorCode.ACTIVITY_NOT_IN_COLLECTION,
                        "Activity ID: " + activityId + " does not belong to Collection ID: " + collectionId
                );
            }
        }

        // Validate duplicated IDs in request
        Set<String> duplicates = findDuplicates(newOrderList);
        if (!duplicates.isEmpty()) {
            throw new ApplicationException(
                    ErrorCode.DUPLICATE_ACTIVITY_ID,
                    "Duplicate activity IDs found: " + String.join(", ", duplicates)
            );
        }

        // Validate missing activity IDs
        Set<String> missing = new HashSet<>(currentActivityIds);
        newOrderList.forEach(missing::remove);
        if (!missing.isEmpty()) {
            throw new ApplicationException(
                    ErrorCode.MISSING_ACTIVITY_ID,
                    "Missing activity IDs: " + String.join(", ", missing)
            );
        }

        // Fetch all activities from DB
        List<Activity> activities = activityRepository.findAllById(newOrderList);

        Map<String, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getActivityId, Function.identity()));

        List<ReorderedActivityResponse> updatedActivities = new ArrayList<>();

        // Update orderIndex if changed
        for (int newIndex = 0; newIndex < newOrderList.size(); newIndex++) {
            String activityId = newOrderList.get(newIndex);
            Activity activity = activityMap.get(activityId);

            if (activity == null) {
                throw new ApplicationException(
                        ErrorCode.ACTIVITY_NOT_FOUND,
                        "Activity ID: " + activityId + " not found in database"
                );
            }

            if (!Objects.equals(activity.getOrderIndex(), newIndex)) {
                activity.setOrderIndex(newIndex);
                updatedActivities.add(new ReorderedActivityResponse(activityId, newIndex));
            }
        }

        // Save only if any changes
        if (!updatedActivities.isEmpty()) {
            activityRepository.saveAll(
                    updatedActivities.stream()
                            .map(r -> {
                                Activity a = activityMap.get(r.getActivityId());
                                a.setOrderIndex(r.getNewOrderIndex());
                                return a;
                            }).collect(Collectors.toList())
            );
        }

        return updatedActivities;
    }

    private Set<String> findDuplicates(List<String> ids) {
        Set<String> seen = new HashSet<>();
        return ids.stream()
                .filter(id -> !seen.add(id))
                .collect(Collectors.toSet());
    }
}
