package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.constant.SlideElementType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.*;
import com.bitorax.priziq.domain.activity.slide.Slide;
import com.bitorax.priziq.domain.activity.slide.SlideElement;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.UpdateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.*;
import com.bitorax.priziq.dto.request.activity.slide.CreateSlideElementRequest;
import com.bitorax.priziq.dto.request.activity.slide.UpdateSlideElementRequest;
import com.bitorax.priziq.dto.request.activity.slide.UpdateSlideRequest;
import com.bitorax.priziq.dto.response.activity.ActivityDetailResponse;
import com.bitorax.priziq.dto.response.activity.ActivitySummaryResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizMatchingPairAnswerResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizMatchingPairConnectionResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideElementResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityMapper;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.ActivityService;
import com.bitorax.priziq.utils.ActivityUtils;
import com.nimbusds.jose.util.Pair;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityServiceImpl implements ActivityService {
    ActivityRepository activityRepository;
    CollectionRepository collectionRepository;
    QuizRepository quizRepository;
    SlideRepository slideRepository;
    SlideElementRepository slideElementRepository;
    QuizMatchingPairItemRepository quizMatchingPairItemRepository;
    QuizMatchingPairConnectionRepository quizMatchingPairConnectionRepository;
    ActivitySubmissionRepository activitySubmissionRepository;
    ActivityMapper activityMapper;
    ActivityUtils activityUtils;

    @NonFinal @Value("${priziq.quiz.default.time_limit_seconds}") Integer DEFAULT_TIME_LIMIT_SECONDS;
    @NonFinal @Value("${priziq.quiz.default.point_type}") String DEFAULT_POINT_TYPE;
    @NonFinal @Value("${priziq.quiz.matching_pairs.default_question}") String DEFAULT_MATCHING_PAIRS_QUESTION;
    @NonFinal @Value("${priziq.quiz.default.question}") String DEFAULT_QUESTION;
    @NonFinal @Value("${priziq.quiz.choice.option1}") String CHOICE_OPTION1;
    @NonFinal @Value("${priziq.quiz.choice.option2}") String CHOICE_OPTION2;
    @NonFinal @Value("${priziq.quiz.choice.option3}") String CHOICE_OPTION3;
    @NonFinal @Value("${priziq.quiz.choice.option4}") String CHOICE_OPTION4;
    @NonFinal @Value("${priziq.quiz.default_activity.title}") String DEFAULT_ACTIVITY_TITLE;
    @NonFinal @Value("${priziq.quiz.default_activity.description}") String DEFAULT_ACTIVITY_DESCRIPTION;
    @NonFinal @Value("${priziq.quiz.default_activity.is_published}") Boolean DEFAULT_ACTIVITY_IS_PUBLISHED;

    private static final Set<String> VALID_QUIZ_TYPES = Set.of("CHOICE", "REORDER", "TYPE_ANSWER", "TRUE_FALSE", "LOCATION", "MATCHING_PAIRS");

    @Override
    @Transactional
    public ActivitySummaryResponse createActivity(CreateActivityRequest createActivityRequest) {
        Collection currentCollection = collectionRepository
                .findById(createActivityRequest.getCollectionId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLLECTION_NOT_FOUND));

        ActivityType.validateActivityType(createActivityRequest.getActivityType());

        Activity activity = activityMapper.createActivityRequestToActivity(createActivityRequest);
        activity.setCollection(currentCollection);

        int maxOrderIndex = currentCollection.getActivities() != null ?
                currentCollection.getActivities().stream()
                        .map(Activity::getOrderIndex)
                        .filter(Objects::nonNull)
                        .max(Integer::compareTo)
                        .orElse(-1) : -1;

        activity.setOrderIndex(maxOrderIndex + 1);

        Activity savedActivity = activityRepository.save(activity);

        if (savedActivity.getActivityType() == ActivityType.INFO_SLIDE) {
            Slide slide = Slide.builder()
                    .slideId(savedActivity.getActivityId())
                    .activity(savedActivity)
                    .build();
            slideRepository.save(slide);
            savedActivity.setSlide(slide);
        } else if (savedActivity.getActivityType() == ActivityType.QUIZ_MATCHING_PAIRS) {
            // Default value
            Quiz quiz = Quiz.builder()
                    .quizId(savedActivity.getActivityId())
                    .activity(savedActivity)
                    .questionText(DEFAULT_MATCHING_PAIRS_QUESTION)
                    .timeLimitSeconds(DEFAULT_TIME_LIMIT_SECONDS)
                    .pointType(PointType.valueOf(DEFAULT_POINT_TYPE))
                    .build();

            QuizMatchingPairAnswer matchingPairAnswer = activityUtils.createDefaultMatchingPairAnswer(quiz);
            quiz.setQuizMatchingPairAnswer(matchingPairAnswer);

            quizRepository.save(quiz);
            savedActivity.setQuiz(quiz);
        }

        return activityMapper.activityToSummaryResponse(savedActivity);
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(String activityId, UpdateQuizRequest updateQuizRequest) {
        activityUtils.validateActivityOwnership(activityId);

        String requestType = updateQuizRequest.getType();
        if (requestType == null || !VALID_QUIZ_TYPES.contains(requestType.toUpperCase())) {
            throw new ApplicationException(ErrorCode.INVALID_QUIZ_TYPE);
        }

        PointType.validatePointType(updateQuizRequest.getPointType());

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        ActivityType activityType = activity.getActivityType();
        if (!activityType.name().startsWith("QUIZ_")) {
            throw new ApplicationException(ErrorCode.ACTIVITY_NOT_QUIZ_TYPE);
        }

        activityUtils.validateRequestType(updateQuizRequest, activityType);

        Quiz quiz = quizRepository.findById(activityId)
                .orElseGet(() -> {
                    Quiz newQuiz = Quiz.builder()
                            .quizId(activityId)
                            .activity(activity)
                            .build();
                    activity.setQuiz(newQuiz);
                    return newQuiz;
                });

        activityMapper.updateQuizFromRequest(updateQuizRequest, quiz);

        switch (activityType) {
            case QUIZ_BUTTONS:
                UpdateChoiceQuizRequest buttonsRequest = (UpdateChoiceQuizRequest) updateQuizRequest;
                activityUtils.validateQuizButtons(buttonsRequest);
                activityUtils.handleChoiceQuiz(quiz, buttonsRequest);
                break;
            case QUIZ_CHECKBOXES:
                UpdateChoiceQuizRequest checkboxesRequest = (UpdateChoiceQuizRequest) updateQuizRequest;
                activityUtils.handleChoiceQuiz(quiz, checkboxesRequest);
                break;
            case QUIZ_REORDER:
                UpdateReorderQuizRequest reorderRequest = (UpdateReorderQuizRequest) updateQuizRequest;
                activityUtils.handleReorderQuiz(quiz, reorderRequest);
                break;
            case QUIZ_TYPE_ANSWER:
                UpdateTypeAnswerQuizRequest typeAnswerRequest = (UpdateTypeAnswerQuizRequest) updateQuizRequest;
                activityUtils.handleTypeAnswerQuiz(quiz, typeAnswerRequest);
                break;
            case QUIZ_TRUE_OR_FALSE:
                UpdateTrueFalseQuizRequest trueFalseRequest = (UpdateTrueFalseQuizRequest) updateQuizRequest;
                activityUtils.handleTrueFalseQuiz(quiz, trueFalseRequest);
                break;
            case QUIZ_LOCATION:
                UpdateLocationQuizRequest locationRequest = (UpdateLocationQuizRequest) updateQuizRequest;
                activityUtils.handleLocationQuiz(quiz, locationRequest);
                break;
            case QUIZ_MATCHING_PAIRS:
                UpdateMatchingPairQuizRequest matchingRequest = (UpdateMatchingPairQuizRequest) updateQuizRequest;
                activityUtils.handleMatchingPairQuiz(quiz, matchingRequest);
                break;
            default:
                throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }

        quizRepository.save(quiz);
        Quiz updatedQuiz = quizRepository.findById(activityId).orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_NOT_FOUND));

        // Load answer list based on activityType
        if (activityType == ActivityType.QUIZ_LOCATION) {
            Hibernate.initialize(updatedQuiz.getQuizLocationAnswers());
        } else if (activityType == ActivityType.QUIZ_MATCHING_PAIRS) {
            Hibernate.initialize(updatedQuiz.getQuizMatchingPairAnswer());
        } else {
            Hibernate.initialize(updatedQuiz.getQuizAnswers());
        }

        return activityMapper.quizToResponse(updatedQuiz);
    }

    @Override
    @Transactional
    public void deleteActivity(String activityId) {
        activityUtils.validateActivityOwnership(activityId);
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));
        activitySubmissionRepository.deleteByActivityActivityId(activityId);
        activityRepository.delete(activity);
    }

    @Override
    @Transactional
    public SlideResponse updateSlide(String slideId, UpdateSlideRequest updateSlideRequest) {
        activityUtils.validateActivityOwnership(slideId);
        Slide slide = activityUtils.getSlideById(slideId);

        activityMapper.updateSlideFromRequest(updateSlideRequest, slide);
        slideRepository.save(slide);
        Slide updatedSlide = slideRepository.findById(slideId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_NOT_FOUND));
        updatedSlide.getSlideElements().size();
        return activityMapper.slideToResponse(updatedSlide);
    }

    @Override
    @Transactional
    public SlideElementResponse addSlideElement(String slideId, CreateSlideElementRequest createSlideElementRequest) {
        activityUtils.validateActivityOwnership(slideId);

        Slide slide = activityUtils.getSlideById(slideId);

        SlideElementType.validateSlideElementType(createSlideElementRequest.getSlideElementType());
        SlideElement slideElement = activityMapper.createSlideElementRequestToSlideElement(createSlideElementRequest);
        slideElement.setSlide(slide);
        slideElementRepository.save(slideElement);
        return activityMapper.slideElementToResponse(slideElement);
    }

    @Override
    @Transactional
    public SlideElementResponse updateSlideElement(String slideId, String elementId, UpdateSlideElementRequest updateSlideElementRequest) {
        SlideElement slideElement = activityUtils.validateAndGetSlideElement(slideId, elementId);

        SlideElementType.validateSlideElementType(updateSlideElementRequest.getSlideElementType());
        activityMapper.updateSlideElementFromRequest(updateSlideElementRequest, slideElement);
        slideElementRepository.save(slideElement);

        return activityMapper.slideElementToResponse(slideElement);
    }

    @Override
    @Transactional
    public void deleteSlideElement(String slideId, String elementId) {
        SlideElement slideElement = activityUtils.validateAndGetSlideElement(slideId, elementId);
        slideElementRepository.delete(slideElement);
    }

    @Override
    @Transactional
    public ActivitySummaryResponse updateActivity(String activityId, UpdateActivityRequest request) {
        activityUtils.validateActivityOwnership(activityId);
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Initialize quiz answer, location answer, matching pair answer (Hibernate)
        activityUtils.initializeActivityComponents(activity);

        ActivityType oldType = activity.getActivityType();

        Integer originalOrderIndex = activity.getOrderIndex();
        activityMapper.updateActivityFromRequest(request, activity);
        activity.setOrderIndex(originalOrderIndex);

        StringBuilder conversionWarning = new StringBuilder();

        if (request.getActivityType() != null) {
            ActivityType newType;
            try {
                newType = ActivityType.valueOf(request.getActivityType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
            }

            if (newType.name().equals(oldType.name())) {
                throw new ApplicationException(ErrorCode.SAME_ACTIVITY_TYPE);
            }

            activityUtils.handleTypeChange(activity, oldType, newType, conversionWarning);
            activity.setActivityType(newType);
        }

        activityRepository.save(activity);

        // Initialize quiz answer, location answer, matching pair answer (Hibernate)
        activityUtils.initializeActivityComponents(activity);

        ActivitySummaryResponse response = activityMapper.activityToSummaryResponse(activity);
        response.setConversionWarning(!conversionWarning.isEmpty() ? conversionWarning.toString() : null);
        return response;
    }

    @Override
    public ActivityDetailResponse getActivityById(String activityId){
        Activity currentActivity = activityRepository.findById(activityId).orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));
        return activityMapper.activityToDetailResponse(currentActivity);
    }

    // Quiz matching pairs (logic item, connection)
    @Override
    @Transactional
    public QuizMatchingPairAnswerResponse addMatchingPairItem(String quizId) {
        Quiz quiz = activityUtils.validateMatchingPairQuiz(quizId);

        QuizMatchingPairAnswer answer = quiz.getQuizMatchingPairAnswer();
        if (answer == null) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ANSWER_NOT_FOUND);
        }

        // Auto increase display order +1 for left column
        int leftDisplayOrder = quizMatchingPairItemRepository
                .findMaxDisplayOrderByQuizMatchingPairAnswerAndIsLeftColumn(answer, true)
                .orElse(0) + 1;

        // Auto increase display order +1 for right column
        int rightDisplayOrder = quizMatchingPairItemRepository
                .findMaxDisplayOrderByQuizMatchingPairAnswerAndIsLeftColumn(answer, false)
                .orElse(0) + 1;

        // Create the left item
        QuizMatchingPairItem leftItem = QuizMatchingPairItem.builder()
                .quizMatchingPairAnswer(answer)
                .content("Left Item")
                .isLeftColumn(true)
                .displayOrder(leftDisplayOrder)
                .build();

        // Create the right item
        QuizMatchingPairItem rightItem = QuizMatchingPairItem.builder()
                .quizMatchingPairAnswer(answer)
                .content("Right Item")
                .isLeftColumn(false)
                .displayOrder(rightDisplayOrder)
                .build();

        // Save both items
        quizMatchingPairItemRepository.save(leftItem);
        quizMatchingPairItemRepository.save(rightItem);

        return activityMapper.quizMatchingPairAnswerToResponse(answer);
    }

    @Override
    @Transactional
    public QuizMatchingPairAnswerResponse updateAndReorderMatchingPairItem(String quizId, String itemId, UpdateAndReorderMatchingPairItemRequest request) {
        // Check quiz matching pair item and get item
        Pair<Quiz, QuizMatchingPairItem> validated = validateQuizAndItem(quizId, itemId);
        QuizMatchingPairItem item = validated.getRight();

        QuizMatchingPairAnswer answer = item.getQuizMatchingPairAnswer();
        Boolean currentIsLeftColumn = item.getIsLeftColumn();
        Integer currentDisplayOrder = item.getDisplayOrder();

        // Get the new value from the request (null if not provided)
        String newContent = request.getContent();
        Boolean newIsLeftColumn = request.getIsLeftColumn();
        Integer newDisplayOrder = request.getDisplayOrder();

        // Get target value isLeftColumn, displayOrder
        Boolean targetIsLeftColumn = newIsLeftColumn != null ? newIsLeftColumn : currentIsLeftColumn;
        Integer targetDisplayOrder = newDisplayOrder != null ? newDisplayOrder : currentDisplayOrder;

        // Validate displayOrder range
        if (newDisplayOrder != null) {
            int maxDisplayOrder = quizMatchingPairItemRepository
                    .findMaxDisplayOrderByQuizMatchingPairAnswerAndIsLeftColumn(answer, targetIsLeftColumn)
                    .orElse(0);
            if (newDisplayOrder < 1 || (maxDisplayOrder > 0 && newDisplayOrder > maxDisplayOrder)) {
                throw new ApplicationException(ErrorCode.INVALID_QUIZ_MATCHING_PAIR_DISPLAY_ORDER);
            }
        }

        // Delete connection if isLeftColumn or displayOrder changed
        if (!Objects.equals(newIsLeftColumn, currentIsLeftColumn) || !Objects.equals(newDisplayOrder, currentDisplayOrder)) {
            List<QuizMatchingPairConnection> connections = quizMatchingPairConnectionRepository
                    .findByQuizIdAndLeftItemIdOrRightItemId(quizId, itemId);
            if (connections.size() > 1) {
                throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_MULTIPLE_CONNECTIONS);
            }
            if (!connections.isEmpty()) {
                QuizMatchingPairConnection connection = connections.getFirst();
                quizMatchingPairConnectionRepository.delete(connection);
            }
        }

        // Logic to handle change
        if (targetIsLeftColumn != currentIsLeftColumn) {
            // Column changed: decrement in old column, increment in new column
            quizMatchingPairItemRepository.decrementDisplayOrder(answer, currentIsLeftColumn, currentDisplayOrder);
            quizMatchingPairItemRepository.incrementDisplayOrder(answer, targetIsLeftColumn, targetDisplayOrder);
        } else if (newDisplayOrder != null && !newDisplayOrder.equals(currentDisplayOrder)) {
            // Move within the same column: swap displayOrder with target item
            QuizMatchingPairItem targetItem = quizMatchingPairItemRepository
                    .findByQuizMatchingPairAnswerAndIsLeftColumnAndDisplayOrder(answer, currentIsLeftColumn, newDisplayOrder)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_NOT_FOUND));
            targetItem.setDisplayOrder(currentDisplayOrder);
            quizMatchingPairItemRepository.save(targetItem);
        }

        // Update item
        if (newContent != null) {
            item.setContent(newContent);
        }
        if (newIsLeftColumn != null) {
            item.setIsLeftColumn(newIsLeftColumn);
        }
        if (newDisplayOrder != null) {
            item.setDisplayOrder(newDisplayOrder);
        }

        quizMatchingPairItemRepository.save(item);

        // Normalize displayOrder for both columns
        normalizeDisplayOrder(answer);

        return activityMapper.quizMatchingPairAnswerToResponse(answer);
    }

    @Override
    @Transactional
    public void deleteMatchingPairItem(String quizId, String itemId) {
        Pair<Quiz, QuizMatchingPairItem> validated = validateQuizAndItem(quizId, itemId);
        QuizMatchingPairItem item = validated.getRight();

        QuizMatchingPairAnswer answer = item.getQuizMatchingPairAnswer();
        Boolean isLeftColumn = item.getIsLeftColumn();
        Integer displayOrder = item.getDisplayOrder();

        // Delete any associated connection
        deleteAssociatedConnection(quizId, itemId);

        // Decrement the displayOrder of the later items in the same column and delete the item
        quizMatchingPairItemRepository.decrementDisplayOrder(answer, isLeftColumn, displayOrder);
        quizMatchingPairItemRepository.delete(item);
    }

    @Override
    @Transactional
    public QuizMatchingPairConnectionResponse addMatchingPairConnection(String quizId, CreateMatchingPairConnectionRequest request) {
        Quiz quiz = activityUtils.validateMatchingPairQuiz(quizId);
        QuizMatchingPairAnswer answer = quiz.getQuizMatchingPairAnswer();
        if (answer == null) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ANSWER_NOT_FOUND);
        }

        // Validate items
        Pair<Quiz, QuizMatchingPairItem> leftValidated = validateQuizAndItem(quizId, request.getLeftItemId());
        Pair<Quiz, QuizMatchingPairItem> rightValidated = validateQuizAndItem(quizId, request.getRightItemId());
        QuizMatchingPairItem leftItem = leftValidated.getRight();
        QuizMatchingPairItem rightItem = rightValidated.getRight();

        // Check column validity and duplicate connection
        if (!leftItem.getIsLeftColumn() || rightItem.getIsLeftColumn()) {
            throw new ApplicationException(ErrorCode.INVALID_QUIZ_MATCHING_PAIR_ITEM_COLUMN);
        }

        if (quizMatchingPairConnectionRepository.existsByQuizIdAndLeftItemIdAndRightItemId(quizId, request.getLeftItemId(), request.getRightItemId())) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_DUPLICATE_CONNECTION);
        }

        // Validate 1-1 relationship (item column A <-> item column B)
        validateItemOneToOneRelation(quizId, request.getLeftItemId(), request.getRightItemId());

        // Create connection
        QuizMatchingPairConnection connection = QuizMatchingPairConnection.builder()
                .quizMatchingPairAnswer(answer)
                .leftItem(leftItem)
                .rightItem(rightItem)
                .build();

        quizMatchingPairConnectionRepository.save(connection);
        return activityMapper.quizMatchingPairConnectionToResponse(connection);
    }

    @Override
    @Transactional
    public void deleteMatchingPairConnection(String quizId, String connectionId) {
        Quiz quiz = activityUtils.validateMatchingPairQuiz(quizId);

        QuizMatchingPairConnection connection = quizMatchingPairConnectionRepository
                .findById(connectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_CONNECTION_NOT_FOUND));

        if (!connection.getQuizMatchingPairAnswer().getQuiz().getQuizId().equals(quiz.getQuizId())) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_CONNECTION_NOT_BELONG_TO_QUIZ);
        }

        quizMatchingPairConnectionRepository.delete(connection);
    }

    @Transactional
    protected void deleteAssociatedConnection(String quizId, String itemId) {
        List<QuizMatchingPairConnection> connections = quizMatchingPairConnectionRepository
                .findByQuizIdAndLeftItemIdOrRightItemId(quizId, itemId);

        if (connections.size() > 1) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_MULTIPLE_CONNECTIONS);
        }

        if (!connections.isEmpty()) {
            QuizMatchingPairConnection connection = connections.getFirst();
            deleteMatchingPairConnection(quizId, connection.getQuizMatchingPairConnectionId());
        }
    }

    private void normalizeDisplayOrder(QuizMatchingPairAnswer answer) {
        // Normalize left column
        List<QuizMatchingPairItem> leftItems = quizMatchingPairItemRepository
                .findByQuizMatchingPairAnswerAndIsLeftColumnOrderByDisplayOrderAsc(answer, true);
        for (int i = 0; i < leftItems.size(); i++) {
            QuizMatchingPairItem item = leftItems.get(i);
            if (item.getDisplayOrder() != i + 1) {
                item.setDisplayOrder(i + 1);
                quizMatchingPairItemRepository.save(item);
            }
        }

        // Normalize right column
        List<QuizMatchingPairItem> rightItems = quizMatchingPairItemRepository
                .findByQuizMatchingPairAnswerAndIsLeftColumnOrderByDisplayOrderAsc(answer, false);
        for (int i = 0; i < rightItems.size(); i++) {
            QuizMatchingPairItem item = rightItems.get(i);
            if (item.getDisplayOrder() != i + 1) {
                item.setDisplayOrder(i + 1);
                quizMatchingPairItemRepository.save(item);
            }
        }
    }

    private void validateItemOneToOneRelation(String quizId, String leftItemId, String rightItemId) {
        // Validate 1-1 relationship (item column A <-> item column B)
        List<QuizMatchingPairConnection> existingConnections = quizMatchingPairConnectionRepository
                .findByQuizIdAndLeftItemIdOrRightItemId(quizId, leftItemId);
        if (!existingConnections.isEmpty()) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_ALREADY_CONNECTED);
        }
        existingConnections = quizMatchingPairConnectionRepository
                .findByQuizIdAndLeftItemIdOrRightItemId(quizId, rightItemId);
        if (!existingConnections.isEmpty()) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_ALREADY_CONNECTED);
        }
    }

    private Pair<Quiz, QuizMatchingPairItem> validateQuizAndItem(String quizId, String itemId) {
        Quiz quiz = activityUtils.validateMatchingPairQuiz(quizId);

        QuizMatchingPairItem item = quizMatchingPairItemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_NOT_FOUND));
        if (!item.getQuizMatchingPairAnswer().getQuiz().getQuizId().equals(quizId)) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_NOT_BELONG_TO_QUIZ);
        }

        return Pair.of(quiz, item);
    }

    @Transactional
    public void createDefaultQuizButtonsActivity(String collectionId) {
        CreateActivityRequest request = CreateActivityRequest.builder()
                .collectionId(collectionId)
                .activityType("QUIZ_BUTTONS")
                .title(DEFAULT_ACTIVITY_TITLE)
                .description(DEFAULT_ACTIVITY_DESCRIPTION)
                .isPublished(DEFAULT_ACTIVITY_IS_PUBLISHED)
                .build();

        ActivitySummaryResponse activityResponse = createActivity(request);

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