package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.constant.SlideElementType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairAnswer;
import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairConnection;
import com.bitorax.priziq.domain.activity.quiz.QuizMatchingPairItem;
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
import com.bitorax.priziq.dto.response.activity.quiz.QuizMatchingPairConnectionResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizMatchingPairItemResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    QuizMatchingPairAnswerRepository quizMatchingPairAnswerRepository;
    QuizMatchingPairItemRepository quizMatchingPairItemRepository;
    QuizMatchingPairConnectionRepository quizMatchingPairConnectionRepository;
    ActivityMapper activityMapper;
    ActivityUtils activityUtils;

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
                    .questionText("Match each item correctly")
                    .timeLimitSeconds(60)
                    .pointType(PointType.STANDARD)
                    .build();

            QuizMatchingPairAnswer matchingPairAnswer = QuizMatchingPairAnswer.builder()
                    .quiz(quiz)
                    .leftColumnName("Left Column")
                    .rightColumnName("Right Column")
                    .items(new ArrayList<>())
                    .connections(new ArrayList<>())
                    .build();
            quiz.setQuizMatchingPairAnswer(matchingPairAnswer);

            QuizMatchingPairItem leftItem = QuizMatchingPairItem.builder()
                    .quizMatchingPairAnswer(matchingPairAnswer)
                    .content("Item 1")
                    .isLeftColumn(true)
                    .displayOrder(1)
                    .build();
            QuizMatchingPairItem rightItem = QuizMatchingPairItem.builder()
                    .quizMatchingPairAnswer(matchingPairAnswer)
                    .content("Match 1")
                    .isLeftColumn(false)
                    .displayOrder(1)
                    .build();
            matchingPairAnswer.getItems().add(leftItem);
            matchingPairAnswer.getItems().add(rightItem);

            QuizMatchingPairConnection connection = QuizMatchingPairConnection.builder()
                    .quizMatchingPairAnswer(matchingPairAnswer)
                    .leftItem(leftItem)
                    .rightItem(rightItem)
                    .build();
            matchingPairAnswer.getConnections().add(connection);

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
                activityUtils.validateQuizCheckboxes(checkboxesRequest);
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

        if (activity.getActivityType().name().startsWith("QUIZ_")) {
            quizRepository.findById(activityId).ifPresent(quizRepository::delete);
        } else if (activity.getActivityType() == ActivityType.INFO_SLIDE) {
            slideRepository.findById(activityId).ifPresent(slideRepository::delete);
        }

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

        if (activity.getQuiz() != null) {
            Hibernate.initialize(activity.getQuiz().getQuizAnswers());
        }
        if (activity.getSlide() != null) {
            Hibernate.initialize(activity.getSlide());
        }

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

        if (activity.getQuiz() != null) {
            Hibernate.initialize(activity.getQuiz().getQuizAnswers());
        }
        if (activity.getSlide() != null) {
            Hibernate.initialize(activity.getSlide());
        }

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
    public QuizMatchingPairItemResponse addMatchingPairItem(String quizId, CreateMatchingPairItemRequest request) {
        Quiz quiz = activityUtils.validateMatchingPairQuiz(quizId);

        QuizMatchingPairAnswer answer = quiz.getQuizMatchingPairAnswer();
        if (answer == null) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ANSWER_NOT_FOUND);
        }

        // Auto increase display order +1 with ifLeftColumn
        int displayOrder = quizMatchingPairItemRepository
                .findMaxDisplayOrderByQuizMatchingPairAnswerAndIsLeftColumn(answer, request.getIsLeftColumn())
                .orElse(0) + 1;

        QuizMatchingPairItem item = QuizMatchingPairItem.builder()
                .quizMatchingPairAnswer(answer)
                .content(request.getContent())
                .isLeftColumn(request.getIsLeftColumn())
                .displayOrder(displayOrder)
                .build();

        return activityMapper.quizMatchingPairItemToResponse(quizMatchingPairItemRepository.save(item));
    }

    @Override
    @Transactional
    public QuizMatchingPairItemResponse updateAndReorderMatchingPairItem(String quizId, String itemId, UpdateAndReorderMatchingPairItemRequest request) {
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

        // Validate no changes in isLeftColumn and displayOrder
        boolean isLeftColumnUnchanged = newIsLeftColumn != null && newIsLeftColumn.equals(currentIsLeftColumn);
        boolean isDisplayOrderUnchanged = newDisplayOrder != null && newDisplayOrder.equals(currentDisplayOrder);
        if(isLeftColumnUnchanged || isDisplayOrderUnchanged){
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_ALREADY_IN_COLUMN_AND_POSITION);
        }

        // Get target value isLeftColumn, displayOrder
        Boolean targetIsLeftColumn = newIsLeftColumn != null ? newIsLeftColumn : currentIsLeftColumn;
        Integer targetDisplayOrder = newDisplayOrder != null ? newDisplayOrder : currentDisplayOrder;

        // Validate displayOrder range
        if (newDisplayOrder != null) {
            int maxDisplayOrder = quizMatchingPairItemRepository
                    .findMaxDisplayOrderByQuizMatchingPairAnswerAndIsLeftColumn(answer, targetIsLeftColumn)
                    .orElse(0);
            if (newDisplayOrder > maxDisplayOrder + 1) {
                throw new ApplicationException(ErrorCode.INVALID_QUIZ_MATCHING_PAIR_DISPLAY_ORDER);
            }
        }

        // Logic to handle change
        if (targetIsLeftColumn != currentIsLeftColumn) {
            // Column changed: decrement in old column, increment in new column
            quizMatchingPairItemRepository.decrementDisplayOrder(answer, currentIsLeftColumn, currentDisplayOrder);
            quizMatchingPairItemRepository.incrementDisplayOrder(answer, targetIsLeftColumn, targetDisplayOrder);
        } else if (newDisplayOrder != null) {
            // Move within the same column
            int start = Math.min(currentDisplayOrder, newDisplayOrder);
            int end = Math.max(currentDisplayOrder, newDisplayOrder);
            int offset = newDisplayOrder > currentDisplayOrder ? -1 : 1;
            quizMatchingPairItemRepository.adjustDisplayOrderInRange(answer, currentIsLeftColumn, start, end, offset);
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

        // Delete connection if isLeftColumn, displayOrder changed (call API delete connection here)

        return activityMapper.quizMatchingPairItemToResponse(quizMatchingPairItemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteMatchingPairItem(String quizId, String itemId) {
        Pair<Quiz, QuizMatchingPairItem> validated = validateQuizAndItem(quizId, itemId);
        QuizMatchingPairItem item = validated.getRight();

        QuizMatchingPairAnswer answer = item.getQuizMatchingPairAnswer();
        Boolean isLeftColumn = item.getIsLeftColumn();
        Integer displayOrder = item.getDisplayOrder();

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
    public QuizMatchingPairConnectionResponse updateMatchingPairConnection(String quizId, String connectionId, UpdateMatchingPairConnectionRequest request) {
        // Validate quiz, connection existed and belonged to quiz
        Quiz quiz = activityUtils.validateMatchingPairQuiz(quizId);
        QuizMatchingPairConnection connection = quizMatchingPairConnectionRepository
                .findById(connectionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_CONNECTION_NOT_FOUND));

        if (!connection.getQuizMatchingPairAnswer().getQuiz().getQuizId().equals(quiz.getQuizId())) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_CONNECTION_NOT_BELONG_TO_QUIZ);
        }

        // Get current and new values left/right item
        String currentLeftItemId = connection.getLeftItem().getQuizMatchingPairItemId();
        String currentRightItemId = connection.getRightItem().getQuizMatchingPairItemId();
        String newLeftItemId = request.getLeftItemId();
        String newRightItemId = request.getRightItemId();

        // Check no changes left/right item
        boolean isLeftUnchanged = newLeftItemId == null || newLeftItemId.equals(currentLeftItemId);
        boolean isRightUnchanged = newRightItemId == null || newRightItemId.equals(currentRightItemId);
        if (isLeftUnchanged && isRightUnchanged) {
            throw new ApplicationException(ErrorCode.NO_UPDATE_PROVIDED);
        }

        // Validate new items
        QuizMatchingPairItem leftItem = connection.getLeftItem();
        QuizMatchingPairItem rightItem = connection.getRightItem();
        if (newLeftItemId != null) {
            Pair<Quiz, QuizMatchingPairItem> leftValidated = validateQuizAndItem(quizId, newLeftItemId);
            leftItem = leftValidated.getRight();
            if (!leftItem.getIsLeftColumn()) {
                throw new ApplicationException(ErrorCode.INVALID_QUIZ_MATCHING_PAIR_ITEM_COLUMN);
            }
        }
        if (newRightItemId != null) {
            Pair<Quiz, QuizMatchingPairItem> rightValidated = validateQuizAndItem(quizId, newRightItemId);
            rightItem = rightValidated.getRight();
            if (rightItem.getIsLeftColumn()) {
                throw new ApplicationException(ErrorCode.INVALID_QUIZ_MATCHING_PAIR_ITEM_COLUMN);
            }
        }

        // Check duplicate connection
        if (quizMatchingPairConnectionRepository.existsByQuizIdAndLeftItemIdAndRightItemIdExcludingSelf(quizId,
                leftItem.getQuizMatchingPairItemId(),
                rightItem.getQuizMatchingPairItemId(),
                connectionId)
        ) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_DUPLICATE_CONNECTION);
        }

        // Update connection
        if (newLeftItemId != null) {
            connection.setLeftItem(leftItem);
        }
        if (newRightItemId != null) {
            connection.setRightItem(rightItem);
        }

        quizMatchingPairConnectionRepository.save(connection);
        return activityMapper.quizMatchingPairConnectionToResponse(connection);
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
}