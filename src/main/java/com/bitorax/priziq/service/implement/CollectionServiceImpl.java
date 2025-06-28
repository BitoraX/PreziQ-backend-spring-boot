package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.CollectionTopicType;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.*;
import com.bitorax.priziq.domain.activity.slide.Slide;
import com.bitorax.priziq.domain.activity.slide.SlideElement;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
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
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.ActivityService;
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
    SessionRepository sessionRepository;
    ActivitySubmissionRepository activitySubmissionRepository;
    SessionParticipantRepository sessionParticipantRepository;
    ActivityService activityService;
    CollectionMapper collectionMapper;
    SecurityUtils securityUtils;

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
        activityService.createDefaultQuizButtonsActivity(savedCollection.getCollectionId());

        return collectionMapper.collectionToSummaryResponse(savedCollection);
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

        // Calculate total activities in collections
        List<CollectionSummaryResponse> responses = collectionPage.getContent().stream()
                .map(this::mapToSummaryResponseWithTotalActivities)
                .toList();

        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1) // base-index = 0
                        .pageSize(pageable.getPageSize())
                        .totalPages(collectionPage.getTotalPages())
                        .totalElements(collectionPage.getTotalElements())
                        .hasNext(collectionPage.hasNext())
                        .hasPrevious(collectionPage.hasPrevious())
                        .build())
                .content(responses)
                .build();
    }

    @Override
    public CollectionSummaryResponse updateCollectionById(String collectionId, UpdateCollectionRequest updateCollectionRequest){
        // Check owner or admin to access and get the current collection
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
    @Transactional
    public void deleteCollectionById(String collectionId) {
        // Check owner or admin to access and get the current collection
        validateCollectionOwnership(collectionId);
        Collection currentCollection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        // Delete ActivitySubmissions related to Activities in the Collection
        List<Activity> activities = currentCollection.getActivities();
        for (Activity activity : activities) {
            activitySubmissionRepository.deleteByActivityActivityId(activity.getActivityId());
        }

        // Delete SessionParticipants and their ActivitySubmissions
        List<Session> sessions = sessionRepository.findByCollectionCollectionId(collectionId);
        for (Session session : sessions) {
            List<SessionParticipant> participants = sessionParticipantRepository.findBySessionSessionId(session.getSessionId());
            for (SessionParticipant participant : participants) {
                activitySubmissionRepository.deleteBySessionParticipantSessionParticipantId(participant.getSessionParticipantId());
            }
            sessionParticipantRepository.deleteBySessionSessionId(session.getSessionId());
        }

        sessionRepository.deleteByCollectionCollectionId(collectionId);
        collectionRepository.delete(currentCollection);
    }

    @Override
    @Transactional
    public List<ReorderedActivityResponse> reorderActivities(String collectionId, ActivityReorderRequest activityReorderRequest) {
        // Check owner or admin to access and get the current collection
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
                    // Group all isPublished = true into a PUBLISH and base topic
                    String groupKey = CollectionTopicType.PUBLISH.name(); // Always add to PUBLISH
                    CollectionSummaryResponse summary = mapToSummaryResponseWithTotalActivities(collection);
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
                    CollectionSummaryResponse summary = mapToSummaryResponseWithTotalActivities(collection);
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

    @Override
    @Transactional
    public CollectionSummaryResponse copyCollection(String collectionId) {
        // Get the current user
        User currentUser = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        // Find the collection to be copied
        Collection sourceCollection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        // Check if the collection is published if it belongs to another user
        if (!Objects.equals(sourceCollection.getCreator().getUserId(), currentUser.getUserId())
                && !sourceCollection.getIsPublished()) {
            throw new ApplicationException(ErrorCode.COLLECTION_NOT_PUBLISHED);
        }

        // Create a copy of the collection
        Collection newCollection = Collection.builder()
                .title(sourceCollection.getTitle())
                .description(sourceCollection.getDescription())
                .isPublished(false) // The copied collection is not published by default
                .coverImage(sourceCollection.getCoverImage())
                .defaultBackgroundMusic(sourceCollection.getDefaultBackgroundMusic())
                .topic(sourceCollection.getTopic())
                .creator(currentUser)
                .activities(new ArrayList<>())
                .build();

        Collection savedCollection = collectionRepository.save(newCollection);

        // Copy activities
        List<Activity> sourceActivities = sourceCollection.getActivities();
        for (Activity sourceActivity : sourceActivities) {
            // Create a new activity
            CreateActivityRequest activityRequest = CreateActivityRequest.builder()
                    .collectionId(savedCollection.getCollectionId())
                    .activityType(sourceActivity.getActivityType().name())
                    .title(sourceActivity.getTitle())
                    .description(sourceActivity.getDescription())
                    .isPublished(sourceActivity.getIsPublished())
                    .build();

            ActivitySummaryResponse activityResponse = activityService.createActivity(activityRequest);
            Activity newActivity = activityRepository.findById(activityResponse.getActivityId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

            // Copy quiz if present, reuse existing Quiz if available
            if (sourceActivity.getQuiz() != null) {
                Quiz sourceQuiz = sourceActivity.getQuiz();
                Quiz newQuiz = newActivity.getQuiz(); // Check for existing Quiz
                if (newQuiz == null) {
                    // If no Quiz exists, create a new one with initialized lists
                    newQuiz = Quiz.builder()
                            .quizId(newActivity.getActivityId())
                            .activity(newActivity)
                            .quizAnswers(new ArrayList<>())
                            .quizLocationAnswers(new ArrayList<>())
                            .build();
                    newActivity.setQuiz(newQuiz);
                }
                // Update Quiz properties
                newQuiz.setQuestionText(sourceQuiz.getQuestionText());
                newQuiz.setTimeLimitSeconds(sourceQuiz.getTimeLimitSeconds());
                newQuiz.setPointType(sourceQuiz.getPointType());

                // Initialize quizAnswers if null before clearing
                if (newQuiz.getQuizAnswers() == null) {
                    newQuiz.setQuizAnswers(new ArrayList<>());
                }
                newQuiz.getQuizAnswers().clear();
                for (QuizAnswer sourceAnswer : sourceQuiz.getQuizAnswers()) {
                    QuizAnswer newAnswer = QuizAnswer.builder()
                            .quiz(newQuiz)
                            .answerText(sourceAnswer.getAnswerText())
                            .isCorrect(sourceAnswer.getIsCorrect())
                            .explanation(sourceAnswer.getExplanation())
                            .orderIndex(sourceAnswer.getOrderIndex())
                            .build();
                    newQuiz.getQuizAnswers().add(newAnswer);
                }

                // Initialize quizLocationAnswers if null before clearing
                if (newQuiz.getQuizLocationAnswers() == null) {
                    newQuiz.setQuizLocationAnswers(new ArrayList<>());
                }
                newQuiz.getQuizLocationAnswers().clear();
                for (QuizLocationAnswer sourceLocationAnswer : sourceQuiz.getQuizLocationAnswers()) {
                    QuizLocationAnswer newLocationAnswer = QuizLocationAnswer.builder()
                            .quiz(newQuiz)
                            .longitude(sourceLocationAnswer.getLongitude())
                            .latitude(sourceLocationAnswer.getLatitude())
                            .radius(sourceLocationAnswer.getRadius())
                            .build();
                    newQuiz.getQuizLocationAnswers().add(newLocationAnswer);
                }

                // Copy quiz matching pair answer if present, reuse existing if available
                if (sourceQuiz.getQuizMatchingPairAnswer() != null) {
                    QuizMatchingPairAnswer sourceMatchingPairAnswer = sourceQuiz.getQuizMatchingPairAnswer();
                    QuizMatchingPairAnswer newMatchingPairAnswer = newQuiz.getQuizMatchingPairAnswer();
                    if (newMatchingPairAnswer == null) {
                        newMatchingPairAnswer = QuizMatchingPairAnswer.builder()
                                .quiz(newQuiz)
                                .quizMatchingPairAnswerId(newQuiz.getQuizId()) // Ensure ID matches Quiz
                                .items(new ArrayList<>())
                                .connections(new ArrayList<>())
                                .build();
                        newQuiz.setQuizMatchingPairAnswer(newMatchingPairAnswer);
                    }
                    // Update properties
                    newMatchingPairAnswer.setLeftColumnName(sourceMatchingPairAnswer.getLeftColumnName());
                    newMatchingPairAnswer.setRightColumnName(sourceMatchingPairAnswer.getRightColumnName());

                    // Initialize items and connections if null before clearing
                    if (newMatchingPairAnswer.getItems() == null) {
                        newMatchingPairAnswer.setItems(new ArrayList<>());
                    }
                    if (newMatchingPairAnswer.getConnections() == null) {
                        newMatchingPairAnswer.setConnections(new ArrayList<>());
                    }
                    newMatchingPairAnswer.getItems().clear();
                    newMatchingPairAnswer.getConnections().clear();

                    // Copy items and create an item map for connections
                    Map<String, QuizMatchingPairItem> itemMap = new HashMap<>();
                    for (QuizMatchingPairItem sourceItem : sourceMatchingPairAnswer.getItems()) {
                        QuizMatchingPairItem newItem = QuizMatchingPairItem.builder()
                                .quizMatchingPairAnswer(newMatchingPairAnswer)
                                .content(sourceItem.getContent())
                                .isLeftColumn(sourceItem.getIsLeftColumn())
                                .displayOrder(sourceItem.getDisplayOrder())
                                .build();
                        newMatchingPairAnswer.getItems().add(newItem);
                        itemMap.put(sourceItem.getQuizMatchingPairItemId(), newItem);
                    }

                    // Copy connections
                    for (QuizMatchingPairConnection sourceConnection : sourceMatchingPairAnswer.getConnections()) {
                        QuizMatchingPairItem newLeftItem = itemMap.get(sourceConnection.getLeftItem().getQuizMatchingPairItemId());
                        QuizMatchingPairItem newRightItem = itemMap.get(sourceConnection.getRightItem().getQuizMatchingPairItemId());
                        if (newLeftItem != null && newRightItem != null) {
                            QuizMatchingPairConnection newConnection = QuizMatchingPairConnection.builder()
                                    .quizMatchingPairAnswer(newMatchingPairAnswer)
                                    .leftItem(newLeftItem)
                                    .rightItem(newRightItem)
                                    .build();
                            newMatchingPairAnswer.getConnections().add(newConnection);
                        }
                    }
                } else {
                    // Remove QuizMatchingPairAnswer if a source doesn't have it
                    if (newQuiz.getQuizMatchingPairAnswer() != null) {
                        newQuiz.setQuizMatchingPairAnswer(null);
                    }
                }
            } else {
                // Remove Quiz if a source doesn't have it
                if (newActivity.getQuiz() != null) {
                    newActivity.setQuiz(null);
                }
            }

            // Copy slide if present, reuse existing Slide if available
            if (sourceActivity.getSlide() != null) {
                Slide sourceSlide = sourceActivity.getSlide();
                Slide newSlide = newActivity.getSlide(); // Check for existing Slide
                if (newSlide == null) {
                    newSlide = Slide.builder()
                            .slideId(newActivity.getActivityId())
                            .activity(newActivity)
                            .slideElements(new ArrayList<>())
                            .build();
                    newActivity.setSlide(newSlide);
                }
                // Update Slide properties
                newSlide.setTransitionEffect(sourceSlide.getTransitionEffect());
                newSlide.setTransitionDuration(sourceSlide.getTransitionDuration());
                newSlide.setAutoAdvanceSeconds(sourceSlide.getAutoAdvanceSeconds());

                // Initialize slideElements if null before clearing
                if (newSlide.getSlideElements() == null) {
                    newSlide.setSlideElements(new ArrayList<>());
                }
                newSlide.getSlideElements().clear();
                for (SlideElement sourceElement : sourceSlide.getSlideElements()) {
                    SlideElement newElement = SlideElement.builder()
                            .slide(newSlide)
                            .slideElementType(sourceElement.getSlideElementType())
                            .positionX(sourceElement.getPositionX())
                            .positionY(sourceElement.getPositionY())
                            .width(sourceElement.getWidth())
                            .height(sourceElement.getHeight())
                            .rotation(sourceElement.getRotation())
                            .layerOrder(sourceElement.getLayerOrder())
                            .content(sourceElement.getContent())
                            .sourceUrl(sourceElement.getSourceUrl())
                            .entryAnimation(sourceElement.getEntryAnimation())
                            .entryAnimationDuration(sourceElement.getEntryAnimationDuration())
                            .entryAnimationDelay(sourceElement.getEntryAnimationDelay())
                            .exitAnimation(sourceElement.getExitAnimation())
                            .exitAnimationDuration(sourceElement.getExitAnimationDuration())
                            .exitAnimationDelay(sourceElement.getExitAnimationDelay())
                            .build();
                    newSlide.getSlideElements().add(newElement);
                }
            } else {
                // Remove Slide if a source doesn't have it
                if (newActivity.getSlide() != null) {
                    newActivity.setSlide(null);
                }
            }

            // Set orderIndex and other activity properties
            newActivity.setOrderIndex(sourceActivity.getOrderIndex());
            newActivity.setBackgroundColor(sourceActivity.getBackgroundColor());
            newActivity.setBackgroundImage(sourceActivity.getBackgroundImage());

            // Save the activity once to persist all changes via cascading
            activityRepository.save(newActivity);
        }

        return collectionMapper.collectionToSummaryResponse(savedCollection);
    }

    private void validateCollectionOwnership(String collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        User currentUser = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        // Check if a user has an ADMIN role. If not admin, verify ownership
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

    private CollectionSummaryResponse mapToSummaryResponseWithTotalActivities(Collection collection) {
        CollectionSummaryResponse dto = collectionMapper.collectionToSummaryResponse(collection);
        dto.setTotalActivities(collection.getActivities().size());
        return dto;
    }
}