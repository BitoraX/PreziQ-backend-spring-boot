package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.*;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityMapper;
import com.bitorax.priziq.repository.ActivityRepository;
import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.repository.QuizAnswerRepository;
import com.bitorax.priziq.repository.QuizRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityServiceImp implements ActivityService {
    ActivityRepository activityRepository;
    CollectionRepository collectionRepository;
    QuizRepository quizRepository;
    QuizAnswerRepository quizAnswerRepository;
    ActivityMapper activityMapper;

    @Override
    public ActivityResponse createActivity(CreateActivityRequest createActivityRequest){
        Collection currentCollection = collectionRepository
                .findById(createActivityRequest.getCollectionId())
                .orElseThrow(() -> new AppException(ErrorCode.COLLECTION_NOT_FOUND));

        ActivityType.validateActivityType(createActivityRequest.getActivityType());

        // Mapper other fields and set collection
        Activity activity = activityMapper.createActivityRequestToActivity(createActivityRequest);
        activity.setCollection(currentCollection);

        // Handle increase order index of activity (max + 1)
        Integer maxOrderIndex = currentCollection.getActivities().stream()
                .map(Activity::getOrderIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(-1);

        activity.setOrderIndex(maxOrderIndex + 1); // index final

        // Save and return response
        return activityMapper.activityToResponse(activityRepository.save(activity));
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(String activityId, UpdateQuizRequest updateQuizRequest) {
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_NOT_FOUND));

        ActivityType activityType = activity.getActivityType();
        if (!activityType.name().startsWith("QUIZ_")) {
            throw new AppException(ErrorCode.ACTIVITY_NOT_QUIZ_TYPE);
        }

        validateRequestType(updateQuizRequest, activityType);

        Quiz quiz = activity.getQuiz();
        if (quiz == null) {
            quiz = Quiz.builder()
                    .quizId(activityId)
                    .activity(activity)
                    .build();
            activity.setQuiz(quiz);
        } else {
            quiz = quizRepository.findById(activityId).orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));
        }

        activityMapper.updateQuizFromRequest(updateQuizRequest, quiz);

        switch (activityType) {
            case QUIZ_BUTTONS:
                UpdateChoiceQuizRequest buttonsRequest = (UpdateChoiceQuizRequest) updateQuizRequest;
                validateQuizButtons(buttonsRequest);
                handleChoiceQuiz(quiz, buttonsRequest, activityType);
                break;
            case QUIZ_CHECKBOXES:
                UpdateChoiceQuizRequest checkboxesRequest = (UpdateChoiceQuizRequest) updateQuizRequest;
                validateQuizCheckboxes(checkboxesRequest);
                handleChoiceQuiz(quiz, checkboxesRequest, activityType);
                break;
            case QUIZ_REORDER:
                UpdateReorderQuizRequest reorderRequest = (UpdateReorderQuizRequest) updateQuizRequest;
                validateQuizReorder(reorderRequest);
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

        quizRepository.save(quiz);
        return activityMapper.quizToResponse(quiz);
    }

    private void validateRequestType(UpdateQuizRequest request, ActivityType activityType) {
        switch (activityType) {
            case QUIZ_BUTTONS:
            case QUIZ_CHECKBOXES:
                if (!(request instanceof UpdateChoiceQuizRequest)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_REORDER:
                if (!(request instanceof UpdateReorderQuizRequest)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_TYPE_ANSWER:
                if (!(request instanceof UpdateTypeAnswerQuizRequest)) {
                    throw new AppException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_TRUE_OR_FALSE:
                if (!(request instanceof UpdateTrueFalseQuizRequest)) {
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

    private void validateQuizReorder(UpdateReorderQuizRequest request) {
        if (request.getCorrectOrder().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_QUIZ_REORDER_ANSWERS);
        }
    }

    private void handleChoiceQuiz(Quiz quiz, UpdateChoiceQuizRequest request, ActivityType type) {
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
        if (quiz.getQuizAnswers() == null) {
            quiz.setQuizAnswers(new ArrayList<>());
        } else {
            quiz.getQuizAnswers().clear();
        }
        quiz.getQuizAnswers().addAll(newAnswers);
    }
}
