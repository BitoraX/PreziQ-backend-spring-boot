package com.bitorax.priziq.utils;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.*;
import com.bitorax.priziq.domain.activity.slide.Slide;
import com.bitorax.priziq.domain.activity.slide.SlideElement;
import com.bitorax.priziq.dto.request.activity.quiz.*;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
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

    @NonFinal @Value("${priziq.quiz.default.question}") String DEFAULT_QUESTION;
    @NonFinal @Value("${priziq.quiz.reorder.step1}") String REORDER_STEP1;
    @NonFinal @Value("${priziq.quiz.reorder.step2}") String REORDER_STEP2;
    @NonFinal @Value("${priziq.quiz.choice.option1}") String CHOICE_OPTION1;
    @NonFinal @Value("${priziq.quiz.choice.option2}") String CHOICE_OPTION2;
    @NonFinal @Value("${priziq.quiz.choice.wrong_answer}") String CHOICE_WRONG_ANSWER;
    @NonFinal @Value("${priziq.quiz.type_answer.default}") String TYPE_ANSWER_DEFAULT;
    @NonFinal @Value("${priziq.quiz.true_false.option_true}") String CHOICE_TRUE;
    @NonFinal @Value("${priziq.quiz.true_false.option_false}") String CHOICE_FALSE;
    @NonFinal @Value("${priziq.quiz.default.time_limit_seconds}") Integer DEFAULT_TIME_LIMIT_SECONDS;
    @NonFinal @Value("${priziq.quiz.default.point_type}") String DEFAULT_POINT_TYPE;
    @NonFinal @Value("${priziq.slide.default.transition_duration}") BigDecimal DEFAULT_TRANSITION_DURATION;
    @NonFinal @Value("${priziq.slide.default.auto_advance_seconds}") Integer DEFAULT_AUTO_ADVANCE_SECONDS;
    @NonFinal @Value("${priziq.quiz.location.default_longitude}") Double DEFAULT_LONGITUDE;
    @NonFinal @Value("${priziq.quiz.location.default_latitude}") Double DEFAULT_LATITUDE;
    @NonFinal @Value("${priziq.quiz.location.default_radius}") Double DEFAULT_RADIUS;
    @NonFinal @Value("${priziq.quiz.matching_pairs.left_column}") String DEFAULT_LEFT_COLUMN;
    @NonFinal @Value("${priziq.quiz.matching_pairs.right_column}") String DEFAULT_RIGHT_COLUMN;
    @NonFinal @Value("${priziq.quiz.matching_pairs.item1}") String DEFAULT_ITEM1;
    @NonFinal @Value("${priziq.quiz.matching_pairs.match1}") String DEFAULT_MATCH1;

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
            case QUIZ_MATCHING_PAIRS:
                if (!requestType.equals("MATCHING_PAIRS") || !(request instanceof UpdateMatchingPairQuizRequest)) {
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
            answers.add(QuizAnswer.builder()
                    .quiz(quiz)
                    .answerText(answerReq.getAnswerText())
                    .isCorrect(answerReq.getIsCorrect())
                    .explanation(answerReq.getExplanation())
                    .orderIndex(i)
                    .build());
        }
        updateQuizAnswers(quiz, answers);
    }

    public void handleReorderQuiz(Quiz quiz, UpdateReorderQuizRequest request) {
        List<QuizAnswer> answers = new ArrayList<>();
        for (int i = 0; i < request.getCorrectOrder().size(); i++) {
            answers.add(QuizAnswer.builder()
                    .quiz(quiz)
                    .answerText(request.getCorrectOrder().get(i))
                    .isCorrect(true)
                    .orderIndex(i)
                    .build());
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
        List<QuizAnswer> answers = createDefaultBinaryAnswers(quiz, CHOICE_TRUE, CHOICE_FALSE);
        answers.get(0).setIsCorrect(request.getCorrectAnswer());
        answers.get(1).setIsCorrect(!request.getCorrectAnswer());
        updateQuizAnswers(quiz, answers);
    }

    public void handleMatchingPairQuiz(Quiz quiz, UpdateMatchingPairQuizRequest request) {
        QuizMatchingPairAnswer answer = quiz.getQuizMatchingPairAnswer() != null ?
                quiz.getQuizMatchingPairAnswer() :
                QuizMatchingPairAnswer.builder().quiz(quiz).build();
        answer.setLeftColumnName(request.getLeftColumnName());
        answer.setRightColumnName(request.getRightColumnName());
        quiz.setQuizMatchingPairAnswer(answer);
    }

    public void handleLocationQuiz(Quiz quiz, UpdateLocationQuizRequest request) {
        List<QuizLocationAnswer> locationAnswers = new ArrayList<>();
        for (int i = 0; i < request.getLocationAnswers().size(); i++) {
            LocationAnswerRequest answerReq = request.getLocationAnswers().get(i);
            locationAnswers.add(QuizLocationAnswer.builder()
                    .quiz(quiz)
                    .longitude(answerReq.getLongitude())
                    .latitude(answerReq.getLatitude())
                    .radius(answerReq.getRadius())
                    .build());
        }
        updateQuizLocationAnswers(quiz, locationAnswers);
    }

    public void updateQuizLocationAnswers(Quiz quiz, List<QuizLocationAnswer> newAnswers) {
        if (quiz.getQuizLocationAnswers() == null) {
            quiz.setQuizLocationAnswers(new ArrayList<>());
        }
        quiz.getQuizLocationAnswers().clear();
        quiz.getQuizLocationAnswers().addAll(newAnswers);
    }

    public void updateQuizAnswers(Quiz quiz, List<QuizAnswer> newAnswers) {
        if (quiz.getQuizAnswers() == null) {
            quiz.setQuizAnswers(new ArrayList<>());
        }
        quiz.getQuizAnswers().clear();
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
                .quizLocationAnswers(new ArrayList<>())
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

    private List<QuizAnswer> createDefaultBinaryAnswers(Quiz quiz, String correctText, String incorrectText) {
        List<QuizAnswer> answers = new ArrayList<>();
        answers.add(QuizAnswer.builder().quiz(quiz).answerText(correctText).isCorrect(true).orderIndex(0).build());
        answers.add(QuizAnswer.builder().quiz(quiz).answerText(incorrectText).isCorrect(false).orderIndex(1).build());
        return answers;
    }

    private List<QuizAnswer> createDefaultReorderAnswers(Quiz quiz) {
        List<QuizAnswer> answers = new ArrayList<>();
        answers.add(QuizAnswer.builder().quiz(quiz).answerText(REORDER_STEP1).isCorrect(true).orderIndex(0).build());
        answers.add(QuizAnswer.builder().quiz(quiz).answerText(REORDER_STEP2).isCorrect(true).orderIndex(1).build());
        return answers;
    }

    private List<QuizAnswer> createDefaultTypeAnswer(Quiz quiz) {
        List<QuizAnswer> answers = new ArrayList<>();
        answers.add(QuizAnswer.builder().quiz(quiz).answerText(TYPE_ANSWER_DEFAULT).isCorrect(true).orderIndex(0).build());
        return answers;
    }

    private List<QuizLocationAnswer> createDefaultLocationAnswers(Quiz quiz) {
        List<QuizLocationAnswer> locationAnswers = new ArrayList<>();
        locationAnswers.add(QuizLocationAnswer.builder()
                .quiz(quiz)
                .longitude(DEFAULT_LONGITUDE)
                .latitude(DEFAULT_LATITUDE)
                .radius(DEFAULT_RADIUS)
                .build());
        return locationAnswers;
    }

    public QuizMatchingPairAnswer createDefaultMatchingPairAnswer(Quiz quiz) {
        QuizMatchingPairAnswer matchingPairAnswer = QuizMatchingPairAnswer.builder()
                .quiz(quiz)
                .leftColumnName(DEFAULT_LEFT_COLUMN)
                .rightColumnName(DEFAULT_RIGHT_COLUMN)
                .items(new ArrayList<>())
                .connections(new ArrayList<>())
                .build();

        QuizMatchingPairItem leftItem = QuizMatchingPairItem.builder()
                .quizMatchingPairAnswer(matchingPairAnswer)
                .content(DEFAULT_ITEM1)
                .isLeftColumn(true)
                .displayOrder(1)
                .build();
        QuizMatchingPairItem rightItem = QuizMatchingPairItem.builder()
                .quizMatchingPairAnswer(matchingPairAnswer)
                .content(DEFAULT_MATCH1)
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

        return matchingPairAnswer;
    }

    private void clearQuizData(Quiz quiz) {
        if (quiz.getQuizAnswers() == null) {
            quiz.setQuizAnswers(new ArrayList<>());
        } else {
            quiz.getQuizAnswers().clear();
        }
        if (quiz.getQuizLocationAnswers() == null) {
            quiz.setQuizLocationAnswers(new ArrayList<>());
        } else {
            quiz.getQuizLocationAnswers().clear();
        }
        quiz.setQuizMatchingPairAnswer(null);
    }

    private void convertToReorder(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        if (answers.size() < 2) {
            quiz.getQuizAnswers().addAll(createDefaultReorderAnswers(quiz));
            warning.append("Insufficient answers, created default reorder answers");
        } else {
            answers.forEach(answer -> answer.setIsCorrect(true));
            quiz.getQuizAnswers().addAll(answers);
        }
    }

    public void convertQuizType(Quiz quiz, ActivityType oldType, ActivityType newType, StringBuilder warning) {
        List<QuizAnswer> answers = quiz.getQuizAnswers() != null ? quiz.getQuizAnswers() : new ArrayList<>();
        List<QuizLocationAnswer> locationAnswers = quiz.getQuizLocationAnswers() != null ? quiz.getQuizLocationAnswers() : new ArrayList<>();
        QuizMatchingPairAnswer matchingPairAnswer = quiz.getQuizMatchingPairAnswer();
        String questionText = quiz.getQuestionText() != null ? quiz.getQuestionText() : DEFAULT_QUESTION;

        QuizAnswer correctAnswer = answers.stream().filter(QuizAnswer::getIsCorrect).findFirst().orElse(null);
        QuizAnswer firstAnswer = answers.isEmpty() ? null : answers.getFirst();

        clearQuizData(quiz);

        switch (oldType) {
            case QUIZ_BUTTONS:
                switch (newType) {
                    case QUIZ_CHECKBOXES -> quiz.getQuizAnswers().addAll(answers);
                    case QUIZ_REORDER -> convertToReorder(answers, quiz, warning);
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(correctAnswer, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(correctAnswer, quiz, warning);
                    case QUIZ_LOCATION -> convertToLocation(answers, locationAnswers, quiz, warning);
                    case QUIZ_MATCHING_PAIRS -> convertToMatchingPairs(answers, quiz, warning);
                }
                break;
            case QUIZ_CHECKBOXES:
                switch (newType) {
                    case QUIZ_BUTTONS -> {
                        reduceToSingleCorrect(answers, warning);
                        quiz.getQuizAnswers().addAll(answers);
                    }
                    case QUIZ_REORDER -> convertToReorder(answers, quiz, warning);
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(correctAnswer, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(correctAnswer, quiz, warning);
                    case QUIZ_LOCATION -> convertToLocation(answers, locationAnswers, quiz, warning);
                    case QUIZ_MATCHING_PAIRS -> convertToMatchingPairs(answers, quiz, warning);
                }
                break;
            case QUIZ_REORDER:
                switch (newType) {
                    case QUIZ_BUTTONS -> {
                        reduceToSingleCorrect(answers, warning);
                        quiz.getQuizAnswers().addAll(answers);
                    }
                    case QUIZ_CHECKBOXES -> quiz.getQuizAnswers().addAll(answers);
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(firstAnswer, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(firstAnswer, quiz, warning);
                    case QUIZ_LOCATION -> convertToLocation(answers, locationAnswers, quiz, warning);
                    case QUIZ_MATCHING_PAIRS -> convertToMatchingPairs(answers, quiz, warning);
                }
                break;
            case QUIZ_TYPE_ANSWER:
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> {
                        addDefaultOptionsIfEmpty(answers, quiz, warning);
                        quiz.getQuizAnswers().addAll(answers);
                    }
                    case QUIZ_REORDER -> {
                        ensureReorderCompatibility(answers, quiz, warning);
                        quiz.getQuizAnswers().addAll(answers);
                    }
                    case QUIZ_TRUE_OR_FALSE -> updateToTrueFalse(firstAnswer, quiz, warning);
                    case QUIZ_LOCATION -> convertToLocation(answers, locationAnswers, quiz, warning);
                    case QUIZ_MATCHING_PAIRS -> convertToMatchingPairs(answers, quiz, warning);
                }
                break;
            case QUIZ_TRUE_OR_FALSE:
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> quiz.getQuizAnswers().addAll(answers);
                    case QUIZ_REORDER -> convertToReorder(answers, quiz, warning);
                    case QUIZ_TYPE_ANSWER -> updateToSingleAnswer(correctAnswer, quiz, warning);
                    case QUIZ_LOCATION -> convertToLocation(answers, locationAnswers, quiz, warning);
                    case QUIZ_MATCHING_PAIRS -> convertToMatchingPairs(answers, quiz, warning);
                }
                break;
            case QUIZ_LOCATION:
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> convertFromLocationToChoice(locationAnswers, quiz, warning);
                    case QUIZ_REORDER -> convertFromLocationToReorder(locationAnswers, quiz, warning);
                    case QUIZ_TYPE_ANSWER -> convertFromLocationToTypeAnswer(locationAnswers, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> convertFromLocationToTrueFalse(locationAnswers, quiz, warning);
                    case QUIZ_MATCHING_PAIRS -> convertFromLocationToMatchingPairs(locationAnswers, quiz, warning);
                }
                break;
            case QUIZ_MATCHING_PAIRS:
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> convertFromMatchingPairsToChoice(matchingPairAnswer, quiz, warning);
                    case QUIZ_REORDER -> convertFromMatchingPairsToReorder(matchingPairAnswer, quiz, warning);
                    case QUIZ_TYPE_ANSWER -> convertFromMatchingPairsToTypeAnswer(matchingPairAnswer, quiz, warning);
                    case QUIZ_TRUE_OR_FALSE -> convertFromMatchingPairsToTrueFalse(matchingPairAnswer, quiz, warning);
                    case QUIZ_LOCATION -> convertFromMatchingPairsToLocation(matchingPairAnswer, quiz, warning);
                }
                break;
            case INFO_SLIDE:
                switch (newType) {
                    case QUIZ_BUTTONS, QUIZ_CHECKBOXES -> {
                        quiz.getQuizAnswers().addAll(createDefaultBinaryAnswers(quiz, CHOICE_OPTION1, CHOICE_OPTION2));
                        warning.append("Created default multiple-choice question with options");
                    }
                    case QUIZ_REORDER -> {
                        quiz.getQuizAnswers().addAll(createDefaultReorderAnswers(quiz));
                        warning.append("Created default reorder question");
                    }
                    case QUIZ_TYPE_ANSWER -> {
                        quiz.getQuizAnswers().addAll(createDefaultTypeAnswer(quiz));
                        warning.append("Created default type-answer question");
                    }
                    case QUIZ_TRUE_OR_FALSE -> {
                        quiz.getQuizAnswers().addAll(createDefaultBinaryAnswers(quiz, CHOICE_TRUE, CHOICE_FALSE));
                        warning.append("Created default True/False question");
                    }
                    case QUIZ_LOCATION -> {
                        quiz.getQuizLocationAnswers().addAll(createDefaultLocationAnswers(quiz));
                        warning.append("Created default location quiz");
                    }
                    case QUIZ_MATCHING_PAIRS -> {
                        quiz.setQuizMatchingPairAnswer(createDefaultMatchingPairAnswer(quiz));
                        warning.append("Created default matching pair");
                    }
                }
                break;
            default:
                throw new IllegalStateException("The previous activity type is undefined: " + oldType);
        }
        quiz.setQuestionText(questionText);
    }

    private void convertToLocation(List<QuizAnswer> answers, List<QuizLocationAnswer> locationAnswers, Quiz quiz, StringBuilder warning) {
        if (!locationAnswers.isEmpty()) {
            quiz.getQuizLocationAnswers().addAll(locationAnswers);
        } else if (!answers.isEmpty()) {
            quiz.getQuizLocationAnswers().addAll(createDefaultLocationAnswers(quiz));
            warning.append("Converted answers to default location answers");
        } else {
            quiz.getQuizLocationAnswers().addAll(createDefaultLocationAnswers(quiz));
            warning.append("Created default location answer");
        }
    }

    private void convertToMatchingPairs(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        QuizMatchingPairAnswer answer = createDefaultMatchingPairAnswer(quiz);
        if (!answers.isEmpty()) {
            List<QuizMatchingPairItem> items = answer.getItems();
            items.clear();
            for (int i = 0; i < Math.min(answers.size(), 2); i++) {
                items.add(QuizMatchingPairItem.builder()
                        .quizMatchingPairAnswer(answer)
                        .content(answers.get(i).getAnswerText())
                        .isLeftColumn(i % 2 == 0)
                        .displayOrder(1)
                        .build());
            }
            if (items.size() >= 2) {
                answer.getConnections().add(QuizMatchingPairConnection.builder()
                        .quizMatchingPairAnswer(answer)
                        .leftItem(items.get(0))
                        .rightItem(items.get(1))
                        .build());
            }
            warning.append("Converted answers to matching pair items");
        }
        quiz.setQuizMatchingPairAnswer(answer);
    }

    private void convertFromLocationToChoice(List<QuizLocationAnswer> locationAnswers, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = new ArrayList<>();
        if (!locationAnswers.isEmpty()) {
            for (int i = 0; i < locationAnswers.size(); i++) {
                QuizLocationAnswer loc = locationAnswers.get(i);
                answers.add(QuizAnswer.builder()
                        .quiz(quiz)
                        .answerText(String.format("Location: long=%s, lat=%s", loc.getLongitude(), loc.getLatitude()))
                        .isCorrect(i == 0)
                        .orderIndex(i)
                        .build());
            }
            warning.append("Converted location answers to choice answers");
        } else {
            answers = createDefaultBinaryAnswers(quiz, CHOICE_OPTION1, CHOICE_OPTION2);
            warning.append("Created default multiple-choice question with options");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromLocationToReorder(List<QuizLocationAnswer> locationAnswers, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = new ArrayList<>();
        if (!locationAnswers.isEmpty()) {
            for (int i = 0; i < locationAnswers.size(); i++) {
                QuizLocationAnswer loc = locationAnswers.get(i);
                answers.add(QuizAnswer.builder()
                        .quiz(quiz)
                        .answerText(String.format("Step %d: long=%s, lat=%s", i + 1, loc.getLongitude(), loc.getLatitude()))
                        .isCorrect(true)
                        .orderIndex(i)
                        .build());
            }
            warning.append("Converted location answers to reorder answers");
        } else {
            answers = createDefaultReorderAnswers(quiz);
            warning.append("Created default reorder question");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromLocationToTypeAnswer(List<QuizLocationAnswer> locationAnswers, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = new ArrayList<>();
        if (!locationAnswers.isEmpty()) {
            QuizLocationAnswer loc = locationAnswers.getFirst();
            answers.add(QuizAnswer.builder()
                    .quiz(quiz)
                    .answerText(String.format("long=%s, lat=%s", loc.getLongitude(), loc.getLatitude()))
                    .isCorrect(true)
                    .orderIndex(0)
                    .build());
            warning.append("Converted first location answer to type-answer");
        } else {
            answers = createDefaultTypeAnswer(quiz);
            warning.append("Created default type-answer question");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromLocationToTrueFalse(List<QuizLocationAnswer> locationAnswers, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = createDefaultBinaryAnswers(quiz, CHOICE_TRUE, CHOICE_FALSE);
        if (!locationAnswers.isEmpty()) {
            answers.get(0).setIsCorrect(true);
            answers.get(1).setIsCorrect(false);
            warning.append("Converted location to True/False with default true answer");
        } else {
            warning.append("Created default True/False question");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromLocationToMatchingPairs(List<QuizLocationAnswer> locationAnswers, Quiz quiz, StringBuilder warning) {
        QuizMatchingPairAnswer answer = createDefaultMatchingPairAnswer(quiz);
        if (!locationAnswers.isEmpty()) {
            List<QuizMatchingPairItem> items = answer.getItems();
            items.clear();
            for (int i = 0; i < Math.min(locationAnswers.size(), 2); i++) {
                QuizLocationAnswer loc = locationAnswers.get(i);
                items.add(QuizMatchingPairItem.builder()
                        .quizMatchingPairAnswer(answer)
                        .content(String.format("long=%s, lat=%s", loc.getLongitude(), loc.getLatitude()))
                        .isLeftColumn(i % 2 == 0)
                        .displayOrder(1)
                        .build());
            }
            if (items.size() >= 2) {
                answer.getConnections().add(QuizMatchingPairConnection.builder()
                        .quizMatchingPairAnswer(answer)
                        .leftItem(items.get(0))
                        .rightItem(items.get(1))
                        .build());
            }
            warning.append("Converted location answers to matching pair items");
        }
        quiz.setQuizMatchingPairAnswer(answer);
    }

    private void convertFromMatchingPairsToChoice(QuizMatchingPairAnswer matchingPairAnswer, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = new ArrayList<>();
        if (matchingPairAnswer != null && !matchingPairAnswer.getItems().isEmpty()) {
            List<QuizMatchingPairItem> items = matchingPairAnswer.getItems();
            for (int i = 0; i < Math.min(items.size(), 2); i++) {
                answers.add(QuizAnswer.builder()
                        .quiz(quiz)
                        .answerText(items.get(i).getContent())
                        .isCorrect(i == 0)
                        .orderIndex(i)
                        .build());
            }
            warning.append("Converted matching pair items to choice answers");
        } else {
            answers = createDefaultBinaryAnswers(quiz, CHOICE_OPTION1, CHOICE_OPTION2);
            warning.append("Created default multiple-choice question with options");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromMatchingPairsToReorder(QuizMatchingPairAnswer matchingPairAnswer, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = new ArrayList<>();
        if (matchingPairAnswer != null && !matchingPairAnswer.getItems().isEmpty()) {
            List<QuizMatchingPairItem> items = matchingPairAnswer.getItems();
            for (int i = 0; i < items.size(); i++) {
                answers.add(QuizAnswer.builder()
                        .quiz(quiz)
                        .answerText(items.get(i).getContent())
                        .isCorrect(true)
                        .orderIndex(i)
                        .build());
            }
            warning.append("Converted matching pair items to reorder answers");
        } else {
            answers = createDefaultReorderAnswers(quiz);
            warning.append("Created default reorder question");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromMatchingPairsToTypeAnswer(QuizMatchingPairAnswer matchingPairAnswer, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = new ArrayList<>();
        if (matchingPairAnswer != null && !matchingPairAnswer.getItems().isEmpty()) {
            answers.add(QuizAnswer.builder()
                    .quiz(quiz)
                    .answerText(matchingPairAnswer.getItems().getFirst().getContent())
                    .isCorrect(true)
                    .orderIndex(0)
                    .build());
            warning.append("Converted first matching pair item to type-answer");
        } else {
            answers = createDefaultTypeAnswer(quiz);
            warning.append("Created default type-answer question");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromMatchingPairsToTrueFalse(QuizMatchingPairAnswer matchingPairAnswer, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> answers = createDefaultBinaryAnswers(quiz, CHOICE_TRUE, CHOICE_FALSE);
        if (matchingPairAnswer != null && !matchingPairAnswer.getItems().isEmpty()) {
            answers.get(0).setIsCorrect(matchingPairAnswer.getItems().getFirst().getContent().toLowerCase().contains("true"));
            answers.get(1).setIsCorrect(!answers.get(0).getIsCorrect());
            warning.append("Converted matching pair item to True/False based on content");
        } else {
            warning.append("Created default True/False question");
        }
        quiz.getQuizAnswers().addAll(answers);
    }

    private void convertFromMatchingPairsToLocation(QuizMatchingPairAnswer matchingPairAnswer, Quiz quiz, StringBuilder warning) {
        List<QuizLocationAnswer> locationAnswers = new ArrayList<>();
        if (matchingPairAnswer != null && !matchingPairAnswer.getItems().isEmpty()) {
            for (int i = 0; i < Math.min(matchingPairAnswer.getItems().size(), 1); i++) {
                locationAnswers.add(QuizLocationAnswer.builder()
                        .quiz(quiz)
                        .longitude(DEFAULT_LONGITUDE)
                        .latitude(DEFAULT_LATITUDE)
                        .radius(DEFAULT_RADIUS)
                        .build());
            }
            warning.append("Converted matching pair to default location answer");
        } else {
            locationAnswers = createDefaultLocationAnswers(quiz);
            warning.append("Created default location quiz");
        }
        quiz.getQuizLocationAnswers().addAll(locationAnswers);
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

    public void updateToSingleAnswer(QuizAnswer answer, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> newAnswers = new ArrayList<>();
        if (answer != null) {
            newAnswers.add(QuizAnswer.builder()
                    .quiz(quiz)
                    .answerText(answer.getAnswerText())
                    .isCorrect(true)
                    .orderIndex(0)
                    .build());
        } else {
            newAnswers = createDefaultTypeAnswer(quiz);
            warning.append("No correct answer found, created default type-answer");
        }
        quiz.getQuizAnswers().addAll(newAnswers);
    }

    public void updateToTrueFalse(QuizAnswer answer, Quiz quiz, StringBuilder warning) {
        List<QuizAnswer> newAnswers = createDefaultBinaryAnswers(quiz, CHOICE_TRUE, CHOICE_FALSE);
        if (answer != null) {
            boolean isTrue = answer.getAnswerText().toLowerCase().contains("true");
            newAnswers.get(0).setIsCorrect(isTrue);
            newAnswers.get(1).setIsCorrect(!isTrue);
            warning.append("Converted to True/False based on previous answer");
        } else {
            newAnswers.get(0).setIsCorrect(true);
            newAnswers.get(1).setIsCorrect(false);
            warning.append("Created default True/False question");
        }
        quiz.getQuizAnswers().addAll(newAnswers);
    }

    public void addDefaultOptionsIfEmpty(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        if (answers.isEmpty()) {
            createDefaultBinaryAnswers(quiz, TYPE_ANSWER_DEFAULT, CHOICE_WRONG_ANSWER);
            warning.append("Converted to multiple-choice question with default wrong answer");
        }
    }

    public void ensureReorderCompatibility(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        if (answers.isEmpty()) {
            createDefaultReorderAnswers(quiz);
            warning.append("Added default reorder steps");
        } else {
            answers.forEach(answer -> answer.setIsCorrect(true));
        }
    }

    public void validateActivityOwnership(String activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = securityUtils.isAdmin(user);
        if (!isAdmin && !Objects.equals(activity.getCollection().getCreator().getUserId(), user.getUserId())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    public Slide getSlideById(String slideId) {
        return slideRepository.findById(slideId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_NOT_FOUND));
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

    public Quiz validateMatchingPairQuiz(String quizId) {
        validateActivityOwnership(quizId);

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_NOT_FOUND));
        if (quiz.getActivity().getActivityType() != ActivityType.QUIZ_MATCHING_PAIRS) {
            throw new ApplicationException(ErrorCode.ACTIVITY_NOT_MATCHING_PAIRS);
        }

        return quiz;
    }

    public void initializeActivityComponents(Activity activity) {
        if (activity.getQuiz() != null) {
            Hibernate.initialize(activity.getQuiz().getQuizAnswers());
            Hibernate.initialize(activity.getQuiz().getQuizLocationAnswers());
            Hibernate.initialize(activity.getQuiz().getQuizMatchingPairAnswer());
        }
        if (activity.getSlide() != null) {
            Hibernate.initialize(activity.getSlide());
        }
    }
}