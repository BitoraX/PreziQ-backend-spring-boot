package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.domain.activity.slide.Slide;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.UpdateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.*;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityMapper;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.ActivityService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityServiceImp implements ActivityService {
    ActivityRepository activityRepository;
    CollectionRepository collectionRepository;
    QuizRepository quizRepository;
    SlideRepository slideRepository;
    ActivityMapper activityMapper;

    private static final Set<String> VALID_QUIZ_TYPES = Set.of("CHOICE", "REORDER", "TYPE_ANSWER", "TRUE_FALSE");

    @Override
    public ActivityResponse createActivity(CreateActivityRequest createActivityRequest) {
        Collection currentCollection = collectionRepository
                .findById(createActivityRequest.getCollectionId())
                .orElseThrow(() -> new AppException(ErrorCode.COLLECTION_NOT_FOUND));

        ActivityType.validateActivityType(createActivityRequest.getActivityType());

        Activity activity = activityMapper.createActivityRequestToActivity(createActivityRequest);
        activity.setCollection(currentCollection);

        Integer maxOrderIndex = currentCollection.getActivities().stream()
                .map(Activity::getOrderIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(-1);

        activity.setOrderIndex(maxOrderIndex + 1);

        return activityMapper.activityToResponse(activityRepository.save(activity));
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(String activityId, UpdateQuizRequest updateQuizRequest) {
        // Validate quiz type
        String requestType = updateQuizRequest.getType();
        if (requestType == null || !VALID_QUIZ_TYPES.contains(requestType.toUpperCase())) {
            throw new AppException(ErrorCode.INVALID_QUIZ_TYPE);
        }

        // Validate pointType
        PointType.validatePointType(updateQuizRequest.getPointType());

        // Fetch activity
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));

        ActivityType activityType = activity.getActivityType();
        if (!activityType.name().startsWith("QUIZ_")) {
            throw new AppException(ErrorCode.ACTIVITY_NOT_QUIZ_TYPE);
        }

        // Validate request type matches activity type
        validateRequestType(updateQuizRequest, activityType);

        // Get or create quiz
        Quiz quiz = quizRepository.findById(activityId)
                .orElseGet(() -> {
                    Quiz newQuiz = Quiz.builder()
                            .quizId(activityId)
                            .activity(activity)
                            .build();
                    activity.setQuiz(newQuiz);
                    return newQuiz;
                });

        // Update basic quiz fields from request
        activityMapper.updateQuizFromRequest(updateQuizRequest, quiz);

        // Handle specific quiz types
        switch (activityType) {
            case QUIZ_BUTTONS:
                UpdateChoiceQuizRequest buttonsRequest = (UpdateChoiceQuizRequest) updateQuizRequest;
                validateQuizButtons(buttonsRequest);
                handleChoiceQuiz(quiz, buttonsRequest);
                break;
            case QUIZ_CHECKBOXES:
                UpdateChoiceQuizRequest checkboxesRequest = (UpdateChoiceQuizRequest) updateQuizRequest;
                validateQuizCheckboxes(checkboxesRequest);
                handleChoiceQuiz(quiz, checkboxesRequest);
                break;
            case QUIZ_REORDER:
                UpdateReorderQuizRequest reorderRequest = (UpdateReorderQuizRequest) updateQuizRequest;
                handleReorderQuiz(quiz, reorderRequest);
                break;
            case QUIZ_TYPE_ANSWER:
                UpdateTypeAnswerQuizRequest typeAnswerRequest = (UpdateTypeAnswerQuizRequest) updateQuizRequest;
                handleTypeAnswerQuiz(quiz, typeAnswerRequest);
                break;
            case QUIZ_TRUE_OR_FALSE:
                UpdateTrueFalseQuizRequest trueFalseRequest = (UpdateTrueFalseQuizRequest) updateQuizRequest;
                handleTrueFalseQuiz(quiz, trueFalseRequest);
                break;
            default:
                throw new AppException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }

        // Save and return response
        quizRepository.save(quiz);
        Quiz updatedQuiz = quizRepository.findById(activityId).orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));
        updatedQuiz.getQuizAnswers().size(); // Ensure lazy-loaded quizAnswers are fetched
        return activityMapper.quizToResponse(updatedQuiz);
    }

    private void validateRequestType(UpdateQuizRequest request, ActivityType activityType) {
        String requestType = request.getType().toUpperCase();
        switch (activityType) {
            case QUIZ_BUTTONS:
            case QUIZ_CHECKBOXES:
                if (!requestType.equals("CHOICE") || !(request instanceof UpdateChoiceQuizRequest)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_REORDER:
                if (!requestType.equals("REORDER") || !(request instanceof UpdateReorderQuizRequest)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_TYPE_ANSWER:
                if (!requestType.equals("TYPE_ANSWER") || !(request instanceof UpdateTypeAnswerQuizRequest)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_TRUE_OR_FALSE:
                if (!requestType.equals("TRUE_FALSE") || !(request instanceof UpdateTrueFalseQuizRequest)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            default:
                throw new AppException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }
    }

    private void validateQuizButtons(UpdateChoiceQuizRequest request) {
        long correctCount = request.getAnswers().stream().filter(ChoiceAnswerRequest::getIsCorrect).count();
        if (correctCount != 1) {
            throw new AppException(ErrorCode.INVALID_QUIZ_BUTTONS_ANSWERS);
        }
    }

    private void validateQuizCheckboxes(UpdateChoiceQuizRequest request) {
        long correctCount = request.getAnswers().stream().filter(ChoiceAnswerRequest::getIsCorrect).count();
        if (correctCount < 1) {
            throw new AppException(ErrorCode.INVALID_QUIZ_CHECKBOXES_ANSWERS);
        }
    }

    private void handleChoiceQuiz(Quiz quiz, UpdateChoiceQuizRequest request) {
        List<QuizAnswer> answers = new ArrayList<>();
        for (int i = 0; i < request.getAnswers().size(); i++) {
            ChoiceAnswerRequest answerReq = request.getAnswers().get(i);
            QuizAnswer answer = QuizAnswer.builder()
                    .quiz(quiz)
                    .answerText(answerReq.getAnswerText())
                    .isCorrect(answerReq.getIsCorrect())
                    .explanation(answerReq.getExplanation())
                    .orderIndex(i)
                    .build();
            answers.add(answer);
        }
        updateQuizAnswers(quiz, answers);
    }

    private void handleReorderQuiz(Quiz quiz, UpdateReorderQuizRequest request) {
        List<QuizAnswer> answers = new ArrayList<>();
        for (int i = 0; i < request.getCorrectOrder().size(); i++) {
            QuizAnswer answer = QuizAnswer.builder()
                    .quiz(quiz)
                    .answerText(request.getCorrectOrder().get(i))
                    .isCorrect(true)
                    .orderIndex(i)
                    .build();
            answers.add(answer);
        }
        updateQuizAnswers(quiz, answers);
    }

    private void handleTypeAnswerQuiz(Quiz quiz, UpdateTypeAnswerQuizRequest request) {
        QuizAnswer answer = QuizAnswer.builder()
                .quiz(quiz)
                .answerText(request.getCorrectAnswer())
                .isCorrect(true)
                .orderIndex(0)
                .build();
        updateQuizAnswers(quiz, List.of(answer));
    }

    private void handleTrueFalseQuiz(Quiz quiz, UpdateTrueFalseQuizRequest request) {
        QuizAnswer trueAnswer = QuizAnswer.builder()
                .quiz(quiz)
                .answerText("True")
                .isCorrect(request.getCorrectAnswer())
                .orderIndex(0)
                .build();
        QuizAnswer falseAnswer = QuizAnswer.builder()
                .quiz(quiz)
                .answerText("False")
                .isCorrect(!request.getCorrectAnswer())
                .orderIndex(1)
                .build();
        updateQuizAnswers(quiz, List.of(trueAnswer, falseAnswer));
    }

    private void updateQuizAnswers(Quiz quiz, List<QuizAnswer> newAnswers) {
        if (quiz.getQuizAnswers() != null) {
            quiz.getQuizAnswers().clear();
        } else {
            quiz.setQuizAnswers(new ArrayList<>());
        }
        quiz.getQuizAnswers().addAll(newAnswers);
    }

    @Override
    @Transactional
    public void deleteActivity(String activityId) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));

        if (activity.getActivityType().name().startsWith("QUIZ_")) {
            quizRepository.findById(activityId).ifPresent(quizRepository::delete);
        } else if (activity.getActivityType() == ActivityType.INFO_SLIDE) {
            slideRepository.findById(activityId).ifPresent(slideRepository::delete);
        }

        activityRepository.delete(activity);
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(String activityId, UpdateActivityRequest updateActivityRequest) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));

        if (updateActivityRequest.getActivityType() != null) {
            ActivityType newType;
            try {
                newType = ActivityType.valueOf(updateActivityRequest.getActivityType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_ACTIVITY_TYPE);
            }

            if (newType == activity.getActivityType()) {
                throw new AppException(ErrorCode.ACTIVITY_TYPE_UNCHANGED);
            }

            Integer orderIndex = activity.getOrderIndex();
            Collection collection = activity.getCollection();

            String oldTitle = activity.getTitle();
            String oldDescription = activity.getDescription();
            Boolean oldIsPublished = activity.getIsPublished();
            String oldBackgroundColor = activity.getBackgroundColor();
            String oldBackgroundImage = activity.getBackgroundImage();
            String oldCustomBackgroundMusic = activity.getCustomBackgroundMusic();

            deleteActivity(activityId);

            Activity newActivity = Activity.builder()
                    .activityType(newType)
                    .collection(collection)
                    .orderIndex(orderIndex)
                    .title(updateActivityRequest.getTitle() != null ? updateActivityRequest.getTitle() : oldTitle)
                    .description(updateActivityRequest.getDescription() != null ? updateActivityRequest.getDescription() : oldDescription)
                    .isPublished(updateActivityRequest.getIsPublished() != null ? updateActivityRequest.getIsPublished() : oldIsPublished)
                    .backgroundColor(updateActivityRequest.getBackgroundColor() != null ? updateActivityRequest.getBackgroundColor() : oldBackgroundColor)
                    .backgroundImage(updateActivityRequest.getBackgroundImage() != null ? updateActivityRequest.getBackgroundImage() : oldBackgroundImage)
                    .customBackgroundMusic(updateActivityRequest.getCustomBackgroundMusic() != null ? updateActivityRequest.getCustomBackgroundMusic() : oldCustomBackgroundMusic)
                    .build();

            Activity savedActivity = activityRepository.save(newActivity);
            return activityMapper.activityToResponse(savedActivity);
        }

        activityMapper.updateActivityFromRequest(updateActivityRequest, activity);
        Activity updatedActivity = activityRepository.save(activity);
        return activityMapper.activityToResponse(updatedActivity);
    }
}