package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.CollectionTopicType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.collection.ActivityReorderRequest;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.activity.ActivitySummaryResponse;
import com.bitorax.priziq.dto.response.collection.CollectionDetailResponse;
import com.bitorax.priziq.dto.response.collection.CollectionSummaryResponse;
import com.bitorax.priziq.dto.response.collection.ReorderedActivityResponse;
import com.bitorax.priziq.dto.response.common.PaginationMeta;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.CollectionMapper;
import com.bitorax.priziq.repository.ActivityRepository;
import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.repository.QuizRepository;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.ActivityService;
import com.bitorax.priziq.service.CollectionService;
import com.bitorax.priziq.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    QuizRepository quizRepository;
    ActivityService activityService;
    CollectionMapper collectionMapper;
    SecurityUtils securityUtils;

    @NonFinal
    @Value("${priziq.quiz.default.question}")
    String DEFAULT_QUESTION;

    @NonFinal
    @Value("${priziq.quiz.choice.option1}")
    String CHOICE_OPTION1;

    @NonFinal
    @Value("${priziq.quiz.choice.option2}")
    String CHOICE_OPTION2;

    @NonFinal
    @Value("${priziq.quiz.choice.option3}")
    String CHOICE_OPTION3;

    @NonFinal
    @Value("${priziq.quiz.choice.option4}")
    String CHOICE_OPTION4;

    @NonFinal
    @Value("${priziq.quiz.default.time_limit_seconds}")
    Integer DEFAULT_TIME_LIMIT_SECONDS;

    @NonFinal
    @Value("${priziq.quiz.default.point_type}")
    String DEFAULT_POINT_TYPE;

    @NonFinal
    @Value("${priziq.quiz.default_activity.title}")
    String DEFAULT_ACTIVITY_TITLE;

    @NonFinal
    @Value("${priziq.quiz.default_activity.description}")
    String DEFAULT_ACTIVITY_DESCRIPTION;

    @NonFinal
    @Value("${priziq.quiz.default_activity.is_published}")
    Boolean DEFAULT_ACTIVITY_IS_PUBLISHED;

    @Override
    @Transactional
    public CollectionSummaryResponse createCollection(CreateCollectionRequest createCollectionRequest) {
        CollectionTopicType.validateCollectionTopicType(createCollectionRequest.getTopic());
        Collection collection = collectionMapper.createCollectionRequestToCollection(createCollectionRequest);

        User creator = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        collection.setCreator(creator);

        Collection savedCollection = collectionRepository.save(collection);

        // Create default QUIZ_BUTTONS activity
        createDefaultQuizButtonsActivity(savedCollection.getCollectionId());

        return collectionMapper.collectionToSummaryResponse(savedCollection);
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
    public CollectionDetailResponse getCollectionById(String collectionId){
        return collectionMapper.collectionToDetailResponse(collectionRepository.findById(collectionId).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND)));
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
    public CollectionSummaryResponse updateCollectionById(String collectionId, UpdateCollectionRequest updateCollectionRequest){
        // Check owner or admin to access and get current collection
        validateCollectionOwnership(collectionId);
        Collection currentCollection = this.collectionRepository.findById(collectionId).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        String collectionTopic = updateCollectionRequest.getTopic();
        if(collectionTopic != null){
            CollectionTopicType.validateCollectionTopicType(collectionTopic);
        }

        this.collectionMapper.updateCollectionRequestToCollection(currentCollection, updateCollectionRequest);
        return this.collectionMapper.collectionToSummaryResponse(collectionRepository.save(currentCollection));
    }

    @Override
    public void deleteCollectionById(String collectionId){
        // Check owner or admin to access and get current collection
        validateCollectionOwnership(collectionId);
        Collection currentCollection = this.collectionRepository.findById(collectionId).orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        this.collectionRepository.delete(currentCollection);
    }

    @Override
    @Transactional
    public List<ReorderedActivityResponse> reorderActivities(String collectionId, ActivityReorderRequest activityReorderRequest) {
        // Check owner or admin to access and get current collection
        validateCollectionOwnership(collectionId);
        Collection currentCollection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

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
                throw new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND);
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

    @Override
    public Map<String, List<CollectionSummaryResponse>> getCollectionsGroupedByTopic(Pageable pageable) {
        List<Object[]> results = collectionRepository.findPublishedGroupedByTopic(pageable);

        // Use LinkedHashMap to maintain order (PUBLISH comes first)
        Map<String, List<CollectionSummaryResponse>> resultMap = new LinkedHashMap<>();

        // Group data
        Map<String, List<CollectionSummaryResponse>> grouped = results.stream()
                .map(result -> {
                    Collection collection = (Collection) result[1];
                    // Group all isPublished = true into PUBLISH and base topic
                    String groupKey = CollectionTopicType.PUBLISH.name(); // Always add to PUBLISH
                    CollectionSummaryResponse summary = collectionMapper.collectionToSummaryResponse(collection);
                    return new AbstractMap.SimpleEntry<>(groupKey, summary);
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        // Add other topic groups (isPublished = true) from results
        Map<String, List<CollectionSummaryResponse>> topicGroups = results.stream()
                .map(result -> {
                    Collection collection = (Collection) result[1];
                    String groupKey = ((CollectionTopicType) result[0]).name(); // Group by base topic
                    CollectionSummaryResponse summary = collectionMapper.collectionToSummaryResponse(collection);
                    return new AbstractMap.SimpleEntry<>(groupKey, summary);
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        // Put PUBLISH at the beginning
        if (grouped.containsKey(CollectionTopicType.PUBLISH.name())) {
            resultMap.put(CollectionTopicType.PUBLISH.name(), grouped.get(CollectionTopicType.PUBLISH.name()));
        }

        // Add other topics (do not sort keys, keep natural order)
        topicGroups.forEach((key, value) -> {
            if (!key.equals(CollectionTopicType.PUBLISH.name())) {
                resultMap.put(key, value);
            }
        });

        return resultMap;
    }

    private void validateCollectionOwnership(String collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        User currentUser = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        // Check if user has ADMIN role. If not admin, verify ownership
        boolean isAdmin = securityUtils.isAdmin(currentUser);
        if (!isAdmin && !Objects.equals(collection.getCreator().getUserId(), currentUser.getUserId())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Set<String> findDuplicates(List<String> ids) {
        Set<String> seen = new HashSet<>();
        return ids.stream()
                .filter(id -> !seen.add(id))
                .collect(Collectors.toSet());
    }

    private void createDefaultQuizButtonsActivity(String collectionId) {
        CreateActivityRequest request = CreateActivityRequest.builder()
                .collectionId(collectionId)
                .activityType("QUIZ_BUTTONS")
                .title(DEFAULT_ACTIVITY_TITLE)
                .description(DEFAULT_ACTIVITY_DESCRIPTION)
                .isPublished(DEFAULT_ACTIVITY_IS_PUBLISHED)
                .build();

        ActivitySummaryResponse activityResponse = activityService.createActivity(request);

        Activity activity = activityRepository.findById(activityResponse.getActivityId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        Quiz defaultQuiz = Quiz.builder()
                .quizId(activity.getActivityId())
                .activity(activity)
                .questionText(DEFAULT_QUESTION)
                .timeLimitSeconds(DEFAULT_TIME_LIMIT_SECONDS)
                .pointType(PointType.valueOf(DEFAULT_POINT_TYPE))
                .quizAnswers(new ArrayList<>())
                .build();

        List<QuizAnswer> defaultAnswers = new ArrayList<>();
        defaultAnswers.add(QuizAnswer.builder()
                .quiz(defaultQuiz)
                .answerText(CHOICE_OPTION1)
                .isCorrect(true)
                .orderIndex(0)
                .build());
        defaultAnswers.add(QuizAnswer.builder()
                .quiz(defaultQuiz)
                .answerText(CHOICE_OPTION2)
                .isCorrect(false)
                .orderIndex(1)
                .build());
        defaultAnswers.add(QuizAnswer.builder()
                .quiz(defaultQuiz)
                .answerText(CHOICE_OPTION3)
                .isCorrect(false)
                .orderIndex(2)
                .build());
        defaultAnswers.add(QuizAnswer.builder()
                .quiz(defaultQuiz)
                .answerText(CHOICE_OPTION4)
                .isCorrect(false)
                .orderIndex(3)
                .build());

        defaultQuiz.setQuizAnswers(defaultAnswers);
        activity.setQuiz(defaultQuiz);

        quizRepository.save(defaultQuiz);
    }
}
