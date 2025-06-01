package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.domain.activity.quiz.QuizLocationAnswer;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionSummaryResponse;
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

import java.util.*;

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

    // Record to hold a quiz processing result
    private record QuizResult(boolean isCorrect, int responseScore) {}

    @Override
    @Transactional
    public ActivitySubmissionSummaryResponse createActivitySubmission(CreateActivitySubmissionRequest request, String websocketSessionId) {
        // Validate entities
        Session session = sessionRepository.findBySessionCode(request.getSessionCode())
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

        QuizResult result = switch (activityType) {
            case QUIZ_BUTTONS, QUIZ_TRUE_OR_FALSE -> processQuizButtonsOrTrueFalse(request, quiz);
            case QUIZ_CHECKBOXES -> processQuizCheckboxes(request, quiz);
            case QUIZ_TYPE_ANSWER -> processQuizTypeAnswer(request, quiz);
            case QUIZ_REORDER -> processQuizReorder(request, quiz);
            case QUIZ_LOCATION -> processQuizLocation(request, quiz);
            default -> throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        };

        isCorrect = result.isCorrect();
        responseScore = result.responseScore();

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

        // Adjust the score based on response time if correct and not NO_POINTS
        if (isCorrect && pointType != PointType.NO_POINTS) {
            List<ActivitySubmission> correctSubmissions = activitySubmissionRepository
                    .findBySessionParticipant_Session_SessionIdAndActivity_ActivityIdAndIsCorrect(
                            session.getSessionId(), request.getActivityId(), true);

            // Sort by createdAt (earliest first) and find the index of the current submission
            correctSubmissions.sort(Comparator.comparing(ActivitySubmission::getCreatedAt));
            int rank = correctSubmissions.indexOf(savedSubmission);

            // Fastest gets a full score, others get decremented and update submission with a new score
            responseScore = Math.max(0, responseScore - (rank * timeDecrement));
            savedSubmission.setResponseScore(responseScore);
            savedSubmission = activitySubmissionRepository.save(savedSubmission);
        }

        return activitySubmissionMapper.activitySubmissionToSummaryResponse(savedSubmission);
    }

    private QuizResult processQuizButtonsOrTrueFalse(CreateActivitySubmissionRequest request, Quiz quiz) {
        // Expect answerContent to be a single quizAnswerId
        QuizAnswer selectedAnswer = quiz.getQuizAnswers().stream()
                .filter(a -> a.getQuizAnswerId().equals(request.getAnswerContent()))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.QUIZ_ANSWER_NOT_FOUND));
        boolean isCorrect = selectedAnswer.getIsCorrect();
        int responseScore = isCorrect ? baseScore : 0; // Use baseScore from an environment
        return new QuizResult(isCorrect, responseScore);
    }

    private QuizResult processQuizCheckboxes(CreateActivitySubmissionRequest request, Quiz quiz) {
        // Expect answerContent to be comma-separated quizAnswerIds
        List<String> selectedIds = Arrays.asList(request.getAnswerContent().split(","));
        List<QuizAnswer> correctAnswers = quiz.getQuizAnswers().stream()
                .filter(QuizAnswer::getIsCorrect)
                .toList();
        // Check if selected answers match exactly with correct answers
        boolean isCorrect = selectedIds.size() == correctAnswers.size() &&
                selectedIds.stream().allMatch(id -> correctAnswers.stream()
                        .anyMatch(a -> a.getQuizAnswerId().equals(id)));
        int responseScore = isCorrect ? baseScore : 0; // Use baseScore from an environment
        return new QuizResult(isCorrect, responseScore);
    }

    private QuizResult processQuizTypeAnswer(CreateActivitySubmissionRequest request, Quiz quiz) {
        // Compare answerContent with correct answerText (case-insensitive)
        boolean isCorrect = quiz.getQuizAnswers().stream()
                .filter(QuizAnswer::getIsCorrect)
                .anyMatch(a -> a.getAnswerText().equalsIgnoreCase(request.getAnswerContent()));
        int responseScore = isCorrect ? baseScore : 0; // Use baseScore from an environment
        return new QuizResult(isCorrect, responseScore);
    }

    private QuizResult processQuizReorder(CreateActivitySubmissionRequest request, Quiz quiz) {
        // Expect answerContent to be comma-separated quizAnswerIds in user-defined order
        List<String> userOrderIds = Arrays.asList(request.getAnswerContent().split(","));
        List<QuizAnswer> sortedAnswers = quiz.getQuizAnswers().stream()
                .sorted(Comparator.comparingInt(QuizAnswer::getOrderIndex))
                .toList();
        // Check if user order matches correct order
        boolean isCorrect = userOrderIds.size() == sortedAnswers.size() &&
                userOrderIds.stream()
                        .map(id -> sortedAnswers.get(userOrderIds.indexOf(id)).getQuizAnswerId())
                        .toList()
                        .equals(userOrderIds);
        int responseScore = isCorrect ? baseScore : 0; // Use baseScore from an environment
        return new QuizResult(isCorrect, responseScore);
    }

    private QuizResult processQuizLocation(CreateActivitySubmissionRequest request, Quiz quiz) {
        // Expect answerContent to be "lng1,lat1,lng2,lat2,...,lngN,latN"
        String[] coordinates = request.getAnswerContent().split(",");
        if (coordinates.length % 2 != 0 || coordinates.length < 2) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Answer content must contain at least one pair of longitude and latitude, e.g., 'lng1,lat1'");
        }

        List<QuizLocationAnswer> correctLocations = quiz.getQuizLocationAnswers();
        if (coordinates.length / 2 > correctLocations.size()) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Number of coordinate pairs cannot exceed the number of correct locations");
        }

        try {
            // Parse user coordinates into pairs
            List<double[]> userCoordinates = new ArrayList<>();
            for (int i = 0; i < coordinates.length; i += 2) {
                double longitude = Double.parseDouble(coordinates[i]);
                double latitude = Double.parseDouble(coordinates[i + 1]);

                // Validate longitude and latitude
                if (longitude < -180 || longitude > 180) {
                    throw new ApplicationException(ErrorCode.INVALID_LONGITUDE);
                }
                if (latitude < -90 || latitude > 90) {
                    throw new ApplicationException(ErrorCode.INVALID_LATITUDE);
                }

                userCoordinates.add(new double[]{longitude, latitude});
            }

            // Count correct matches
            List<QuizLocationAnswer> unmatchedLocations = new ArrayList<>(correctLocations);
            int correctCount = 0;

            for (double[] userCoord : userCoordinates) {
                double userLong = userCoord[0];
                double userLat = userCoord[1];

                // Find a matching location
                Iterator<QuizLocationAnswer> iterator = unmatchedLocations.iterator();
                while (iterator.hasNext()) {
                    QuizLocationAnswer location = iterator.next();
                    double distance = calculateHaversineDistance(
                            userLat, userLong,
                            location.getLatitude(), location.getLongitude()
                    );
                    if (distance <= location.getRadius()) {
                        correctCount++;
                        iterator.remove(); // Remove the matched location to prevent reuse
                        break;
                    }
                }
            }

            // Calculate score based on the proportion of correct locations
            boolean isCorrect = correctCount == correctLocations.size();
            double proportionCorrect = (double) correctCount / correctLocations.size();
            int responseScore = (int) Math.floor(baseScore * proportionCorrect); // Round down
            return new QuizResult(isCorrect, responseScore);
        } catch (NumberFormatException e) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Invalid longitude or latitude format");
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in meters
    }
}