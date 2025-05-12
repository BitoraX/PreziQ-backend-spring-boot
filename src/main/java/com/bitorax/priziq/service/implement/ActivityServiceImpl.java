package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.constant.SlideElementType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
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
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideElementResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityMapper;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.ActivityService;
import com.bitorax.priziq.utils.ActivityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

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
    ActivityMapper activityMapper;
    ActivityUtils activityUtils;

    private static final Set<String> VALID_QUIZ_TYPES = Set.of("CHOICE", "REORDER", "TYPE_ANSWER", "TRUE_FALSE");

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
            default:
                throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }

        quizRepository.save(quiz);
        Quiz updatedQuiz = quizRepository.findById(activityId).orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_NOT_FOUND));
        updatedQuiz.getQuizAnswers().size();
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

    @Transactional
    public void createDefaultQuizButtonsActivity(String collectionId) {
        activityUtils.createDefaultQuizButtonsActivity(collectionId, this);
    }
}