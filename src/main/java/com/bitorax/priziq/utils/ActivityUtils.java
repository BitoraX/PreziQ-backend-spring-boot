package com.bitorax.priziq.utils;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.domain.activity.quiz.QuizLocationAnswer;
import com.bitorax.priziq.domain.activity.slide.Slide;
import com.bitorax.priziq.domain.activity.slide.SlideElement;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.*;
import com.bitorax.priziq.dto.response.activity.ActivitySummaryResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.implement.ActivityServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityUtils {

    ActivityRepository activityRepository;
    UserRepository userRepository;
    QuizRepository quizRepository;
    SlideRepository slideRepository;
    SlideElementRepository slideElementRepository;
    SecurityUtils securityUtils;

    @NonFinal
    @Value("${priziq.quiz.default.question}")
    String DEFAULT_QUESTION;

    @NonFinal
    @Value("${priziq.quiz.reorder.step1}")
    String REORDER_STEP1;

    @NonFinal
    @Value("${priziq.quiz.reorder.step2}")
    String REORDER_STEP2;

    @NonFinal
    @Value("${priziq.quiz.reorder.default_step}")
    String REORDER_DEFAULT_STEP;

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
    @Value("${priziq.quiz.choice.wrong_answer}")
    String CHOICE_WRONG_ANSWER;

    @NonFinal
    @Value("${priziq.quiz.type_answer.default}")
    String TYPE_ANSWER_DEFAULT;

    @NonFinal
    @Value("${priziq.quiz.true_false.option_true}")
    String CHOICE_TRUE;

    @NonFinal
    @Value("${priziq.quiz.true_false.option_false}")
    String CHOICE_FALSE;

    @NonFinal
    @Value("${priziq.quiz.default.time_limit_seconds}")
    int DEFAULT_TIME_LIMIT_SECONDS;

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
    boolean DEFAULT_ACTIVITY_IS_PUBLISHED;

    @NonFinal
    @Value("${priziq.slide.default.transition_duration}")
    BigDecimal DEFAULT_TRANSITION_DURATION;

    @NonFinal
    @Value("${priziq.slide.default.auto_advance_seconds}")
    int DEFAULT_AUTO_ADVANCE_SECONDS;

    public void validateRequestType(UpdateQuizRequest request, ActivityType activityType) {
        String requestType = request.getType().toUpperCase();
        switch (activityType) {
            case QUIZ_BUTTONS:
            case QUIZ_CHECKBOXES:
                if (!requestType.equals("CHOICE") || !(request instanceof UpdateChoiceQuizRequest)) {
                    throw new ApplicationException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_REORDER:
                if (!requestType.equals("REORDER") || !(request instanceof UpdateReorderQuizRequest)) {
                    throw new ApplicationException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_TYPE_ANSWER:
                if (!requestType.equals("TYPE_ANSWER") || !(request instanceof UpdateTypeAnswerQuizRequest)) {
                    throw new ApplicationException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_TRUE_OR_FALSE:
                if (!requestType.equals("TRUE_FALSE") || !(request instanceof UpdateTrueFalseQuizRequest)) {
                    throw new ApplicationException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            case QUIZ_LOCATION:
                if (!requestType.equals("LOCATION") || !(request instanceof UpdateLocationQuizRequest)) {
                    throw new ApplicationException(ErrorCode.INVALID_REQUEST_TYPE);
                }
                break;
            default:
                throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }
    }

    public void validateQuizButtons(UpdateChoiceQuizRequest request) {
        long correctCount = request.getAnswers().stream().filter(ChoiceAnswerRequest::getIsCorrect).count();
        if (correctCount != 1) {
            throw new ApplicationException(ErrorCode.INVALID_QUIZ_BUTTONS_ANSWERS);
        }
    }

    public void validateQuizCheckboxes(UpdateChoiceQuizRequest request) {
        long correctCount = request.getAnswers().stream().filter(ChoiceAnswerRequest::getIsCorrect).count();
        if (correctCount < 1) {
            throw new ApplicationException(ErrorCode.INVALID_QUIZ_CHECKBOXES_ANSWERS);
        }
    }

    public void handleChoiceQuiz(Quiz quiz, UpdateChoiceQuizRequest request) {
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

    public void handleReorderQuiz(Quiz quiz, UpdateReorderQuizRequest request) {
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

    public void handleTypeAnswerQuiz(Quiz quiz, UpdateTypeAnswerQuizRequest request) {
        QuizAnswer answer = QuizAnswer.builder()
                .quiz(quiz)
                .answerText(request.getCorrectAnswer())
                .isCorrect(true)
                .orderIndex(0)
                .build();
        updateQuizAnswers(quiz, List.of(answer));
    }

    public void handleTrueFalseQuiz(Quiz quiz, UpdateTrueFalseQuizRequest request) {
        QuizAnswer trueAnswer = QuizAnswer.builder()
                .quiz(quiz)
                .answerText(CHOICE_TRUE)
                .isCorrect(request.getCorrectAnswer())
                .orderIndex(0)
                .build();
        QuizAnswer falseAnswer = QuizAnswer.builder()
                .quiz(quiz)
                .answerText(CHOICE_FALSE)
                .isCorrect(!request.getCorrectAnswer())
                .orderIndex(1)
                .build();
        updateQuizAnswers(quiz, List.of(trueAnswer, falseAnswer));
    }

    public void handleLocationQuiz(Quiz quiz, UpdateLocationQuizRequest request) {
        List<QuizLocationAnswer> locationAnswers = new ArrayList<>();
        for (int i = 0; i < request.getLocationAnswers().size(); i++) {
            LocationAnswerRequest answerReq = request.getLocationAnswers().get(i);
            QuizLocationAnswer locationAnswer = QuizLocationAnswer.builder()
                    .quiz(quiz)
                    .longitude(answerReq.getLongitude())
                    .latitude(answerReq.getLatitude())
                    .radius(answerReq.getRadius())
                    .build();
            locationAnswers.add(locationAnswer);
        }
        updateQuizLocationAnswers(quiz, locationAnswers);
    }

    public void updateQuizLocationAnswers(Quiz quiz, List<QuizLocationAnswer> newAnswers) {
        if (quiz.getQuizAnswers() != null) {
            quiz.getQuizAnswers().clear();
        } else {
            quiz.setQuizAnswers(new ArrayList<>());
        }
        quiz.getQuizLocationAnswers().addAll(newAnswers);
    }

    public void updateQuizAnswers(Quiz quiz, List<QuizAnswer> newAnswers) {
        if (quiz.getQuizAnswers() != null) {
            quiz.getQuizAnswers().clear();
        } else {
            quiz.setQuizAnswers(new ArrayList<>());
        }
        quiz.getQuizAnswers().addAll(newAnswers);
    }

    public void handleTypeChange(Activity activity, ActivityType oldType, ActivityType newType, StringBuilder warning) {
        boolean isOldQuiz = oldType != ActivityType.INFO_SLIDE;
        boolean isNewQuiz = newType != ActivityType.INFO_SLIDE;

        if (isOldQuiz && isNewQuiz) {
            Quiz quiz = activity.getQuiz();
            if (quiz == null) {
                quiz = createDefaultQuiz(activity);
            }
            convertQuizType(quiz, oldType, newType, warning);
            quizRepository.save(quiz);
            activity.setSlide(null);
        } else if (isOldQuiz) {
            if (activity.getQuiz() != null) {
                quizRepository.delete(activity.getQuiz());
            }
            activity.setQuiz(null);
            Slide slide = createDefaultSlide(activity);
            slideRepository.save(slide);
            activity.setSlide(slide);
        } else if (isNewQuiz) {
            if (activity.getSlide() != null) {
                slideRepository.delete(activity.getSlide());
            }
            activity.setSlide(null);
            Quiz quiz = createDefaultQuiz(activity);
            convertQuizType(quiz, oldType, newType, warning);
            quizRepository.save(quiz);
            activity.setQuiz(quiz);
        }
    }

    public Quiz createDefaultQuiz(Activity activity) {
        return Quiz.builder()
                .quizId(activity.getActivityId())
                .activity(activity)
                .questionText(DEFAULT_QUESTION)
                .timeLimitSeconds(DEFAULT_TIME_LIMIT_SECONDS)
                .pointType(PointType.valueOf(DEFAULT_POINT_TYPE))
                .quizAnswers(new ArrayList<>())
                .build();
    }

    public Slide createDefaultSlide(Activity activity) {
        return Slide.builder()
                .slideId(activity.getActivityId())
                .activity(activity)
                .slideElements(new ArrayList<>())
                .transitionDuration(DEFAULT_TRANSITION_DURATION)
                .autoAdvanceSeconds(DEFAULT_AUTO_ADVANCE_SECONDS)
                .build();
    }

    public void convertQuizType(Quiz quiz, ActivityType oldType, ActivityType newType, StringBuilder warning) {
        List<QuizAnswer> answers = quiz.getQuizAnswers() != null ? quiz.getQuizAnswers() : new ArrayList<>();
        String questionText = quiz.getQuestionText() != null ? quiz.getQuestionText() : DEFAULT_QUESTION;

        QuizAnswer correctAnswer = answers.stream().filter(QuizAnswer::getIsCorrect).findFirst().orElse(null);
        QuizAnswer firstAnswer = answers.isEmpty() ? null : answers.getFirst();

        switch (oldType) {
            case QUIZ_BUTTONS:
                switch (newType) {
                    case QUIZ_CHECKBOXES -> {}
                    case QUIZ_REORDER -> {
                        if (answers.isEmpty()) {
                            answers.add(QuizAnswer.builder().quiz(quiz).answerText(REORDER_STEP1).isCorrect(true).orderIndex(0).build());
                            answers.add(QuizAnswer.builder().quiz(quiz).answerText(REORDER_STEP2).isCorrect(true).orderIndex(1).build());
                            warning.append("Created default reorder answers");
                        } else {
                            answers.forEach(answer -> answer.setIsCorrect(true));
                        }
                    }
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(answers, correctAnswer, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(answers, correctAnswer, quiz, warning);
                }
                break;
            case QUIZ_CHECKBOXES:
                switch (newType) {
                    case QUIZ_BUTTONS -> reduceToSingleCorrect(answers, warning);
                    case QUIZ_REORDER -> answers.forEach(answer -> answer.setIsCorrect(true));
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(answers, correctAnswer, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(answers, correctAnswer, quiz, warning);
                }
                break;
            case QUIZ_REORDER:
                switch (newType) {
                    case QUIZ_BUTTONS -> reduceToSingleCorrect(answers, warning);
                    case QUIZ_CHECKBOXES -> {}
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(answers, firstAnswer, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(answers, firstAnswer, quiz, warning);
                }
                break;
            case QUIZ_TYPE_ANSWER:
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> addDefaultOptionsIfEmpty(answers, quiz, warning);
                    case QUIZ_REORDER -> ensureReorderCompatibility(answers, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(answers, firstAnswer, quiz, warning);
                }
                break;
            case QUIZ_TRUE_OR_FALSE:
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> {}
                    case QUIZ_REORDER -> answers.forEach(answer -> answer.setIsCorrect(true));
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(answers, correctAnswer, quiz, warning);
                }
                break;
            case INFO_SLIDE:
                answers.clear();
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> {
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText(CHOICE_OPTION1).isCorrect(true).orderIndex(0).build());
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText(CHOICE_OPTION2).isCorrect(false).orderIndex(1).build());
                        warning.append("Created default multiple-choice question with options");
                    }
                    case QUIZ_REORDER -> {
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText(REORDER_STEP1).isCorrect(true).orderIndex(0).build());
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText(REORDER_STEP2).isCorrect(true).orderIndex(1).build());
                        warning.append("Created default reorder question");
                    }
                    case QUIZ_TYPE_ANSWER -> {
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText(TYPE_ANSWER_DEFAULT).isCorrect(true).orderIndex(0).build());
                        warning.append("Created default type-answer question");
                    }
                    case QUIZ_TRUE_OR_FALSE -> {
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText(CHOICE_TRUE).isCorrect(true).orderIndex(0).build());
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText(CHOICE_FALSE).isCorrect(false).orderIndex(1).build());
                        warning.append("Created default True/False question");
                    }
                }
                break;
            default:
                throw new IllegalStateException("The previous activity type is undefined: " + oldType);
        }
        quiz.setQuestionText(questionText);
        quiz.setQuizAnswers(answers);
    }

    public void reduceToSingleCorrect(List<QuizAnswer> answers, StringBuilder warning) {
        boolean firstCorrectSet = false;
        for (QuizAnswer answer : answers) {
            if (answer.getIsCorrect() && !firstCorrectSet) {
                firstCorrectSet = true;
            } else if (answer.getIsCorrect()) {
                answer.setIsCorrect(false);
                warning.append("Multiple correct answers reduced to one");
            }
        }
    }

    public void updateToSingleAnswer(List<QuizAnswer> answers, QuizAnswer answer, Quiz quiz, StringBuilder warning) {
        answers.clear();
        if (answer != null) {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText(answer.getAnswerText()).isCorrect(true).orderIndex(0).build());
        } else {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText(TYPE_ANSWER_DEFAULT).isCorrect(true).orderIndex(0).build());
            warning.append("No correct answer found, using default answer");
        }
    }

    public void updateToTrueFalse(List<QuizAnswer> answers, QuizAnswer answer, Quiz quiz, StringBuilder warning) {
        answers.clear();
        boolean isTrue = answer != null && answer.getAnswerText().toLowerCase().contains("true");
        answers.add(QuizAnswer.builder().quiz(quiz).answerText(CHOICE_TRUE).isCorrect(isTrue).orderIndex(0).build());
        answers.add(QuizAnswer.builder().quiz(quiz).answerText(CHOICE_FALSE).isCorrect(!isTrue).orderIndex(1).build());
        if (answer != null) warning.append("Converted to True/False based on first answer");
    }

    public void addDefaultOptionsIfEmpty(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        if (answers.isEmpty()) {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText(TYPE_ANSWER_DEFAULT).isCorrect(true).orderIndex(0).build());
            answers.add(QuizAnswer.builder().quiz(quiz).answerText(CHOICE_WRONG_ANSWER).isCorrect(false).orderIndex(1).build());
            warning.append("Converted to multiple-choice question with default wrong answer");
        }
    }

    public void ensureReorderCompatibility(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        if (answers.isEmpty()) {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText(REORDER_DEFAULT_STEP).isCorrect(true).orderIndex(0).build());
            warning.append("Added default step");
        } else {
            answers.forEach(answer -> answer.setIsCorrect(true));
        }
    }

    public void validateActivityOwnership(String activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        User currentUser = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = securityUtils.isAdmin(currentUser);
        if (!isAdmin && !Objects.equals(activity.getCollection().getCreator().getUserId(), currentUser.getUserId())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    public Slide getSlideById(String slideId) {
        return slideRepository.findById(slideId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_NOT_FOUND));
    }

    public void createDefaultQuizButtonsActivity(String collectionId, ActivityServiceImpl activityService) {
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
                .orderIndex(2)
                .build());

        defaultQuiz.setQuizAnswers(defaultAnswers);
        activity.setQuiz(defaultQuiz);

        quizRepository.save(defaultQuiz);
    }

    public SlideElement validateAndGetSlideElement(String slideId, String elementId) {
        validateActivityOwnership(slideId);

        Slide slide = getSlideById(slideId);
        SlideElement slideElement = slideElementRepository.findById(elementId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_ELEMENT_NOT_FOUND));

        if (!slideElement.getSlide().getSlideId().equals(slide.getSlideId())) {
            throw new ApplicationException(ErrorCode.SLIDE_ELEMENT_NOT_BELONG_TO_SLIDE);
        }

        return slideElement;
    }
}