package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivitySubmissionMapper;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.ActivitySubmissionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivitySubmissionServiceImpl implements ActivitySubmissionService {
    ActivitySubmissionRepository activitySubmissionRepository;
    SessionRepository sessionRepository;
    ActivityRepository activityRepository;
    SessionParticipantRepository sessionParticipantRepository;
    ActivitySubmissionMapper activitySubmissionMapper;

    @NonFinal
    @Value("${priziq.submission.base-score}")
    Integer baseScore;

    @NonFinal
    @Value("${priziq.submission.time-decrement}")
    Integer timeDecrement;

    @Override
    @Transactional
    public ActivitySubmissionResponse createActivitySubmission(CreateActivitySubmissionRequest request, String websocketSessionId) {
        // Validate entities
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_NOT_FOUND));
        Activity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACTIVITY_NOT_FOUND));
        SessionParticipant sessionParticipant = sessionParticipantRepository
                .findBySessionAndWebsocketSessionId(session, websocketSessionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SESSION_PARTICIPANT_NOT_FOUND));

        // Check if activity is a quiz
        Quiz quiz = activity.getQuiz();
        if (quiz == null) {
            throw new ApplicationException(ErrorCode.ACTIVITY_NOT_QUIZ_TYPE);
        }

        // Determine isCorrect and score based on activityType
        boolean isCorrect;
        int responseScore;
        ActivityType activityType = activity.getActivityType();

        switch (activityType) {
            case QUIZ_BUTTONS:
            case QUIZ_TRUE_OR_FALSE:
                // Expect answerContent to be a single quizAnswerId
                QuizAnswer selectedAnswer = quiz.getQuizAnswers().stream()
                        .filter(a -> a.getQuizAnswerId().equals(request.getAnswerContent()))
                        .findFirst()
                        .orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_ANSWER_NOT_FOUND));
                isCorrect = selectedAnswer.getIsCorrect();
                responseScore = isCorrect ? baseScore : 0; // Use baseScore from environment
                break;

            case QUIZ_CHECKBOXES:
                // Expect answerContent to be comma-separated quizAnswerIds
                List<String> selectedIds = Arrays.asList(request.getAnswerContent().split(","));
                List<QuizAnswer> correctAnswers = quiz.getQuizAnswers().stream()
                        .filter(QuizAnswer::getIsCorrect)
                        .toList();
                // Check if selected answers match exactly with correct answers
                isCorrect = selectedIds.size() == correctAnswers.size() &&
                        selectedIds.stream().allMatch(id -> correctAnswers.stream()
                                .anyMatch(a -> a.getQuizAnswerId().equals(id)));
                responseScore = isCorrect ? baseScore : 0; // Use baseScore from environment
                break;

            case QUIZ_TYPE_ANSWER:
                // Compare answerContent with correct answerText (case-insensitive)
                isCorrect = quiz.getQuizAnswers().stream()
                        .filter(QuizAnswer::getIsCorrect)
                        .anyMatch(a -> a.getAnswerText().equalsIgnoreCase(request.getAnswerContent()));
                responseScore = isCorrect ? baseScore : 0; // Use baseScore from environment
                break;

            case QUIZ_REORDER:
                // Expect answerContent to be comma-separated quizAnswerIds in user-defined order
                List<String> userOrderIds = Arrays.asList(request.getAnswerContent().split(","));
                List<QuizAnswer> sortedAnswers = quiz.getQuizAnswers().stream()
                        .sorted(Comparator.comparingInt(QuizAnswer::getOrderIndex))
                        .toList();
                // Check if user order matches correct order
                isCorrect = userOrderIds.size() == sortedAnswers.size() &&
                        userOrderIds.stream()
                                .map(id -> sortedAnswers.get(userOrderIds.indexOf(id)).getQuizAnswerId())
                                .toList()
                                .equals(userOrderIds);
                responseScore = isCorrect ? baseScore : 0; // Use baseScore from environment
                break;

            default:
                throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }

        // Adjust score based on PointType
        PointType pointType = quiz.getPointType();
        switch (pointType) {
            case NO_POINTS:
                responseScore = 0;
                break;
            case STANDARD:
                // Keep base score
                break;
            case DOUBLE_POINTS:
                responseScore = responseScore * 2; // Double the base score
                break;
        }

        // Create and save ActivitySubmission to get createdAt
        ActivitySubmission submission = ActivitySubmission.builder()
                .sessionParticipant(sessionParticipant)
                .activity(activity)
                .answerContent(request.getAnswerContent())
                .isCorrect(isCorrect)
                .responseScore(responseScore)
                .build();

        ActivitySubmission savedSubmission = activitySubmissionRepository.save(submission);

        // Adjust score based on response time if correct and not NO_POINTS
        if (isCorrect && pointType != PointType.NO_POINTS) {
            // Find all correct submissions for this activity in this session
            List<ActivitySubmission> correctSubmissions = activitySubmissionRepository
                    .findBySessionParticipant_Session_SessionIdAndActivity_ActivityIdAndIsCorrect(
                            request.getSessionId(), request.getActivityId(), true);

            // Sort by createdAt (earliest first)
            correctSubmissions.sort(Comparator.comparing(ActivitySubmission::getCreatedAt));

            // Find the index of the current submission
            int rank = correctSubmissions.indexOf(savedSubmission);

            // Adjust score: fastest gets full score, others get decremented
            responseScore = Math.max(0, responseScore - (rank * timeDecrement));

            // Update submission with new score
            savedSubmission.setResponseScore(responseScore);
            savedSubmission = activitySubmissionRepository.save(savedSubmission);
        }

        return activitySubmissionMapper.activitySubmissionToResponse(savedSubmission);
    }
}
