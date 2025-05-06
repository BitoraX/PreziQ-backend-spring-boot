package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.constant.SlideElementType;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
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
import com.bitorax.priziq.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    UserRepository userRepository;
    ActivityMapper activityMapper;
    SecurityUtils securityUtils;

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

        Integer maxOrderIndex = currentCollection.getActivities().stream()
                .map(Activity::getOrderIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(-1);

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
        // Check owner or admin to access
        validateActivityOwnership(activityId);

        // Validate quiz type
        String requestType = updateQuizRequest.getType();
        if (requestType == null || !VALID_QUIZ_TYPES.contains(requestType.toUpperCase())) {
            throw new ApplicationException(ErrorCode.INVALID_QUIZ_TYPE);
        }

        // Validate pointType
        PointType.validatePointType(updateQuizRequest.getPointType());

        // Fetch activity
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        ActivityType activityType = activity.getActivityType();
        if (!activityType.name().startsWith("QUIZ_")) {
            throw new ApplicationException(ErrorCode.ACTIVITY_NOT_QUIZ_TYPE);
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
                throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }

        // Save and return response
        quizRepository.save(quiz);
        Quiz updatedQuiz = quizRepository.findById(activityId).orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_NOT_FOUND));
        updatedQuiz.getQuizAnswers().size(); // Ensure lazy-loaded quizAnswers are fetched
        return activityMapper.quizToResponse(updatedQuiz);
    }

    @Override
    @Transactional
    public void deleteActivity(String activityId) {
        // Check owner or admin to access and get current activity
        validateActivityOwnership(activityId);
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
        // Check owner or admin to access and get slide activity
        validateActivityOwnership(slideId);
        Slide slide = slideRepository.findById(slideId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_NOT_FOUND));

        activityMapper.updateSlideFromRequest(updateSlideRequest, slide);
        slideRepository.save(slide);
        Slide updatedSlide = slideRepository.findById(slideId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_NOT_FOUND));
        updatedSlide.getSlideElements().size(); // Fetch lazy-loaded slideElements
        return activityMapper.slideToResponse(updatedSlide);
    }

    @Override
    @Transactional
    public SlideElementResponse addSlideElement(String slideId, CreateSlideElementRequest createSlideElementRequest) {
        // Check owner or admin to access
        validateActivityOwnership(slideId);

        Slide slide = getSlideById(slideId);

        SlideElementType.validateSlideElementType(createSlideElementRequest.getSlideElementType());
        SlideElement slideElement = activityMapper.createSlideElementRequestToSlideElement(createSlideElementRequest);
        slideElement.setSlide(slide);
        slideElementRepository.save(slideElement);
        return activityMapper.slideElementToResponse(slideElement);
    }

    @Override
    @Transactional
    public SlideElementResponse updateSlideElement(String slideId, String elementId, UpdateSlideElementRequest updateSlideElementRequest) {
        // Check owner or admin to access
        validateActivityOwnership(slideId);

        Slide slide = getSlideById(slideId);
        SlideElement slideElement = slideElementRepository.findById(elementId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_ELEMENT_NOT_FOUND));

        if (!slideElement.getSlide().getSlideId().equals(slide.getSlideId())) {
            throw new ApplicationException(ErrorCode.SLIDE_ELEMENT_NOT_BELONG_TO_SLIDE);
        }

        SlideElementType.validateSlideElementType(updateSlideElementRequest.getSlideElementType());
        activityMapper.updateSlideElementFromRequest(updateSlideElementRequest, slideElement);
        slideElementRepository.save(slideElement);
        return activityMapper.slideElementToResponse(slideElement);
    }

    @Override
    @Transactional
    public void deleteSlideElement(String slideId, String elementId) {
        // Check owner or admin to access
        validateActivityOwnership(slideId);

        Slide slide = getSlideById(slideId);
        SlideElement slideElement = slideElementRepository.findById(elementId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_ELEMENT_NOT_FOUND));

        if (!slideElement.getSlide().getSlideId().equals(slide.getSlideId())) {
            throw new ApplicationException(ErrorCode.SLIDE_ELEMENT_NOT_BELONG_TO_SLIDE);
        }

        slideElementRepository.delete(slideElement);
    }

    @Override
    @Transactional
    public ActivitySummaryResponse updateActivity(String activityId, UpdateActivityRequest request) {
        // Check owner or admin to access and get current activity
        validateActivityOwnership(activityId);
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        // Initialize related data to prevent lazy loading issues
        if (activity.getQuiz() != null) {
            Hibernate.initialize(activity.getQuiz().getQuizAnswers());
        }
        if (activity.getSlide() != null) {
            Hibernate.initialize(activity.getSlide());
        }

        ActivityType oldType = activity.getActivityType();

        // Update fields from request but keep orderIndex
        Integer originalOrderIndex = activity.getOrderIndex();
        activityMapper.updateActivityFromRequest(request, activity);
        activity.setOrderIndex(originalOrderIndex);

        StringBuilder conversionWarning = new StringBuilder();

        // Handle activityType change if provided
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

            handleTypeChange(activity, oldType, newType, conversionWarning);
            activity.setActivityType(newType);
        }

        activityRepository.save(activity);

        // Ensure response includes all related data
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

    private Slide getSlideById(String slideId) {
        return slideRepository.findById(slideId).orElseThrow(() -> new ApplicationException(ErrorCode.SLIDE_NOT_FOUND));
    }

    private void validateRequestType(UpdateQuizRequest request, ActivityType activityType) {
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
            default:
                throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }
    }

    private void validateQuizButtons(UpdateChoiceQuizRequest request) {
        long correctCount = request.getAnswers().stream().filter(ChoiceAnswerRequest::getIsCorrect).count();
        if (correctCount != 1) {
            throw new ApplicationException(ErrorCode.INVALID_QUIZ_BUTTONS_ANSWERS);
        }
    }

    private void validateQuizCheckboxes(UpdateChoiceQuizRequest request) {
        long correctCount = request.getAnswers().stream().filter(ChoiceAnswerRequest::getIsCorrect).count();
        if (correctCount < 1) {
            throw new ApplicationException(ErrorCode.INVALID_QUIZ_CHECKBOXES_ANSWERS);
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

    private void handleTypeChange(Activity activity, ActivityType oldType, ActivityType newType, StringBuilder warning) {
        boolean isOldQuiz = oldType != ActivityType.INFO_SLIDE;
        boolean isNewQuiz = newType != ActivityType.INFO_SLIDE;

        if (isOldQuiz && isNewQuiz) {
            // Quiz to Quiz: Retain Quiz and update quizAnswers
            Quiz quiz = activity.getQuiz();
            if (quiz == null) {
                quiz = createDefaultQuiz(activity);
            }
            convertQuizType(quiz, oldType, newType, warning);
            quizRepository.save(quiz);
            activity.setSlide(null);
        } else if (isOldQuiz) {
            // Quiz to Slide: Delete Quiz and create Slide
            if (activity.getQuiz() != null) {
                quizRepository.delete(activity.getQuiz());
            }
            activity.setQuiz(null);
            Slide slide = createDefaultSlide(activity);
            slideRepository.save(slide);
            activity.setSlide(slide);
        } else if (isNewQuiz) {
            // Slide to Quiz: Delete Slide and create Quiz
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

    private Quiz createDefaultQuiz(Activity activity) {
        return Quiz.builder()
                .quizId(activity.getActivityId())
                .activity(activity)
                .questionText("Default question")
                .timeLimitSeconds(30)
                .pointType(PointType.STANDARD)
                .quizAnswers(new ArrayList<>())
                .build();
    }

    private Slide createDefaultSlide(Activity activity) {
        return Slide.builder()
                .slideId(activity.getActivityId())
                .activity(activity)
                .slideElements(new ArrayList<>())
                .transitionDuration(BigDecimal.ONE)
                .autoAdvanceSeconds(0)
                .build();
    }

    // Convert quiz type and adjust answers based on old and new types
    private void convertQuizType(Quiz quiz, ActivityType oldType, ActivityType newType, StringBuilder warning) {
        List<QuizAnswer> answers = quiz.getQuizAnswers() != null ? quiz.getQuizAnswers() : new ArrayList<>();
        String questionText = quiz.getQuestionText() != null ? quiz.getQuestionText() : "Default question";

        QuizAnswer correctAnswer = answers.stream().filter(QuizAnswer::getIsCorrect).findFirst().orElse(null);
        QuizAnswer firstAnswer = answers.isEmpty() ? null : answers.get(0);

        switch (oldType) {
            case QUIZ_BUTTONS:
                switch (newType) {
                    case QUIZ_CHECKBOXES -> {}
                    case QUIZ_REORDER -> {
                        if (answers.isEmpty()) {
                            answers.add(QuizAnswer.builder().quiz(quiz).answerText("Step 1").isCorrect(true).orderIndex(0).build());
                            answers.add(QuizAnswer.builder().quiz(quiz).answerText("Step 2").isCorrect(true).orderIndex(1).build());
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
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText("Option 1").isCorrect(true).orderIndex(0).build());
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText("Option 2").isCorrect(false).orderIndex(1).build());
                        warning.append("Created default multiple-choice question with options");
                    }
                    case QUIZ_REORDER -> {
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText("Step 1").isCorrect(true).orderIndex(0).build());
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText("Step 2").isCorrect(true).orderIndex(1).build());
                        warning.append("Created default reorder question");
                    }
                    case QUIZ_TYPE_ANSWER -> {
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText("Default answer").isCorrect(true).orderIndex(0).build());
                        warning.append("Created default type-answer question");
                    }
                    case QUIZ_TRUE_OR_FALSE -> {
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText("True").isCorrect(true).orderIndex(0).build());
                        answers.add(QuizAnswer.builder().quiz(quiz).answerText("False").isCorrect(false).orderIndex(1).build());
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

    // Reduce multiple correct answers to a single correct answer
    private void reduceToSingleCorrect(List<QuizAnswer> answers, StringBuilder warning) {
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

    private void updateToSingleAnswer(List<QuizAnswer> answers, QuizAnswer answer, Quiz quiz, StringBuilder warning) {
        answers.clear();
        if (answer != null) {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText(answer.getAnswerText()).isCorrect(true).orderIndex(0).build());
        } else {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText("Default answer").isCorrect(true).orderIndex(0).build());
            warning.append("No correct answer found, using default answer");
        }
    }

    private void updateToTrueFalse(List<QuizAnswer> answers, QuizAnswer answer, Quiz quiz, StringBuilder warning) {
        answers.clear();
        boolean isTrue = answer != null && answer.getAnswerText().toLowerCase().contains("true");
        answers.add(QuizAnswer.builder().quiz(quiz).answerText("True").isCorrect(isTrue).orderIndex(0).build());
        answers.add(QuizAnswer.builder().quiz(quiz).answerText("False").isCorrect(!isTrue).orderIndex(1).build());
        if (answer != null) warning.append("Converted to True/False based on first answer");
    }

    private void addDefaultOptionsIfEmpty(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        if (answers.isEmpty()) {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText("Default answer").isCorrect(true).orderIndex(0).build());
            answers.add(QuizAnswer.builder().quiz(quiz).answerText("Wrong answer").isCorrect(false).orderIndex(1).build());
            warning.append("Converted to multiple-choice question with default wrong answer");
        }
    }

    private void ensureReorderCompatibility(List<QuizAnswer> answers, Quiz quiz, StringBuilder warning) {
        if (answers.isEmpty()) {
            answers.add(QuizAnswer.builder().quiz(quiz).answerText("Default step").isCorrect(true).orderIndex(0).build());
            warning.append("Added default step");
        } else {
            answers.forEach(answer -> answer.setIsCorrect(true));
        }
    }

    private void validateActivityOwnership(String activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));

        User currentUser = userRepository.findByEmail(SecurityUtils.getCurrentUserEmailFromJwt())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        // Check if user has ADMIN role. If not admin, verify ownership
        boolean isAdmin = securityUtils.isAdmin(currentUser);
        if (!isAdmin && !Objects.equals(activity.getCollection().getCreator().getUserId(), currentUser.getUserId())) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}