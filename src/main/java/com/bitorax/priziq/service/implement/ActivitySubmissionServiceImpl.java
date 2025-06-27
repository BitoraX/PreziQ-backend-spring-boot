package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.ActivityType;
import com.bitorax.priziq.constant.PointType;
import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.*;
import com.bitorax.priziq.domain.session.ActivitySubmission;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.domain.session.SessionParticipant;
import com.bitorax.priziq.dto.cache.SessionCacheDTO;
import com.bitorax.priziq.dto.cache.SubmissionCacheDTO;
import com.bitorax.priziq.dto.request.session.activity_submission.CreateActivitySubmissionRequest;
import com.bitorax.priziq.dto.response.session.ActivitySubmissionSummaryResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivitySubmissionMapper;
import com.bitorax.priziq.mapper.cache.SessionCacheMapper;
import com.bitorax.priziq.mapper.cache.SubmissionCacheMapper;
import com.bitorax.priziq.repository.*;
import com.bitorax.priziq.service.ActivitySubmissionService;
import com.bitorax.priziq.service.cache.SessionRedisCache;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivitySubmissionServiceImpl implements ActivitySubmissionService {
    ActivitySubmissionRepository activitySubmissionRepository;
    ActivityRepository activityRepository;
    SessionRepository sessionRepository;
    SessionParticipantRepository sessionParticipantRepository;
    ActivitySubmissionMapper activitySubmissionMapper;
    SubmissionCacheMapper submissionCacheMapper;
    SessionCacheMapper sessionCacheMapper;
    SessionRedisCache sessionRedisCache;

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

        // Cache Session after retrieval
        SessionCacheDTO cacheDTO = sessionCacheMapper.sessionToCacheDTO(session);
        sessionRedisCache.cacheSession(session.getSessionId(), cacheDTO);

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
            case QUIZ_MATCHING_PAIRS -> processQuizMatchingPairs(request, quiz);
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
            // Check cache for Submissions before querying DB
            List<SubmissionCacheDTO> cachedSubmissions = sessionRedisCache.getCachedSubmissions(session.getSessionId());
            List<ActivitySubmission> correctSubmissions;
            if (!cachedSubmissions.isEmpty()) {
                correctSubmissions = cachedSubmissions.stream()
                        .map(submissionCacheMapper::submissionCacheDTOToActivitySubmission)
                        .filter(sub -> sub.getActivity().getActivityId().equals(request.getActivityId())
                                && Boolean.TRUE.equals(sub.getIsCorrect()))
                        .collect(Collectors.toList());
            } else {
                correctSubmissions = activitySubmissionRepository
                        .findBySessionParticipant_Session_SessionIdAndActivity_ActivityIdAndIsCorrect(
                                session.getSessionId(), request.getActivityId(), true);

                // Cache Submissions after retrieval
                sessionRedisCache.cacheSubmissions(session.getSessionId(),
                        correctSubmissions.stream()
                                .map(submissionCacheMapper::activitySubmissionToCacheDTO)
                                .collect(Collectors.toList()));
            }

            // Sort by createdAt (earliest first) and find the index of the current submission
            correctSubmissions.sort(Comparator.comparing(ActivitySubmission::getCreatedAt));
            int rank = correctSubmissions.indexOf(savedSubmission);

            // Fastest gets a full score, others get decremented and update submission with a new score
            responseScore = Math.max(0, responseScore - (rank * timeDecrement));
            savedSubmission.setResponseScore(responseScore);
            savedSubmission = activitySubmissionRepository.save(savedSubmission);

            // Update cache for Submissions after score adjustment
            List<ActivitySubmission> allSubmissions = activitySubmissionRepository
                    .findBySessionParticipant_Session_SessionId(session.getSessionId());
            sessionRedisCache.cacheSubmissions(session.getSessionId(),
                    allSubmissions.stream()
                            .map(submissionCacheMapper::activitySubmissionToCacheDTO)
                            .collect(Collectors.toList()));
        }

        return activitySubmissionMapper.activitySubmissionToSummaryResponse(savedSubmission);
    }

    private QuizResult processQuizMatchingPairs(CreateActivitySubmissionRequest request, Quiz quiz) {
        // Validate answerContent
        if (request.getAnswerContent() == null || request.getAnswerContent().trim().isEmpty()) {
            return new QuizResult(false, 0);
        }

        String[] itemIds = request.getAnswerContent().split(",");
        if (itemIds.length % 2 != 0) {
            throw new ApplicationException(ErrorCode.INVALID_MATCHING_PAIR_ANSWER);
        }

        // Validate item IDs
        QuizMatchingPairAnswer matchingPairAnswer = quiz.getQuizMatchingPairAnswer();
        if (matchingPairAnswer == null) {
            throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ANSWER_NOT_FOUND);
        }

        List<QuizMatchingPairItem> items = matchingPairAnswer.getItems();
        Set<String> validItemIds = items.stream().map(QuizMatchingPairItem::getQuizMatchingPairItemId).collect(Collectors.toSet());
        Set<String> uniqueItemIds = new HashSet<>();

        for (String itemId : itemIds) {
            if (!validItemIds.contains(itemId)) {
                throw new ApplicationException(ErrorCode.QUIZ_MATCHING_PAIR_ITEM_NOT_FOUND);
            }
            if (!uniqueItemIds.add(itemId)) {
                throw new ApplicationException(ErrorCode.DUPLICATE_MATCHING_PAIR_ITEM);
            }
        }

        // Validate pairs (left-right)
        List<QuizMatchingPairItem> leftItems = items.stream().filter(QuizMatchingPairItem::getIsLeftColumn).toList();
        List<QuizMatchingPairItem> rightItems = items.stream().filter(item -> !item.getIsLeftColumn()).toList();
        Set<String> leftItemIds = leftItems.stream().map(QuizMatchingPairItem::getQuizMatchingPairItemId).collect(Collectors.toSet());
        Set<String> rightItemIds = rightItems.stream().map(QuizMatchingPairItem::getQuizMatchingPairItemId).collect(Collectors.toSet());

        List<String[]> userPairs = new ArrayList<>();
        for (int i = 0; i < itemIds.length; i += 2) {
            String leftId = itemIds[i];
            String rightId = itemIds[i + 1];
            if (!leftItemIds.contains(leftId) || !rightItemIds.contains(rightId)) {
                throw new ApplicationException(ErrorCode.INVALID_MATCHING_PAIR_COLUMN);
            }
            userPairs.add(new String[]{leftId, rightId});
        }

        // Compare with correct connections
        List<QuizMatchingPairConnection> correctConnections = matchingPairAnswer.getConnections();
        if (correctConnections.isEmpty()) {
            throw new ApplicationException(ErrorCode.NO_CORRECT_MATCHING_PAIR_CONNECTIONS);
        }

        int correctCount = 0;
        for (String[] userPair : userPairs) {
            String userLeftId = userPair[0];
            String userRightId = userPair[1];
            boolean isPairCorrect = correctConnections.stream().anyMatch(conn ->
                    conn.getLeftItem().getQuizMatchingPairItemId().equals(userLeftId) &&
                            conn.getRightItem().getQuizMatchingPairItemId().equals(userRightId));
            if (isPairCorrect) {
                correctCount++;
            }
        }

        // Calculate score
        boolean isCorrect = correctCount == correctConnections.size();
        double proportionCorrect = (double) correctCount / correctConnections.size();
        int responseScore = (int) Math.floor(baseScore * proportionCorrect);

        return new QuizResult(isCorrect, responseScore);
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
        String[] selectedIds = request.getAnswerContent().split(",");
        List<QuizAnswer> correctAnswers = quiz.getQuizAnswers().stream()
                .filter(QuizAnswer::getIsCorrect)
                .toList();

        // Count correct matches
        int correctCount = 0;
        for (String selectedId : selectedIds) {
            if (correctAnswers.stream().anyMatch(a -> a.getQuizAnswerId().equals(selectedId))) {
                correctCount++;
            }
        }

        // Calculate score based on the proportion of correct answers
        boolean isCorrect = correctCount == correctAnswers.size();
        double proportionCorrect = (double) correctCount / correctAnswers.size();
        int responseScore = (int) Math.floor(baseScore * proportionCorrect); // Round down
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
        // Check if user order matches the correct order
        boolean isCorrect = userOrderIds.size() == sortedAnswers.size() &&
                userOrderIds.stream()
                        .map(id -> sortedAnswers.get(userOrderIds.indexOf(id)).getQuizAnswerId())
                        .toList()
                        .equals(userOrderIds);
        int responseScore = isCorrect ? baseScore : 0; // Use baseScore from an environment
        return new QuizResult(isCorrect, responseScore);
    }

    private QuizResult processQuizLocation(CreateActivitySubmissionRequest request, Quiz quiz) {
        // Handle empty answerContent (no answers submitted)
        if (request.getAnswerContent() == null || request.getAnswerContent().trim().isEmpty()) {
            return new QuizResult(false, 0);
        }

        // Expect answerContent to be "lng1,lat1,lng2,lat2,...,lngN,latN"
        String[] coordinates = request.getAnswerContent().split(",");
        if (coordinates.length % 2 != 0 || coordinates.length < 2) {
            throw new ApplicationException(ErrorCode.MISSING_LAT_LNG_PAIR);
        }

        // Check if number of coordinates exceeds correct locations
        List<QuizLocationAnswer> correctLocations = quiz.getQuizLocationAnswers();
        if (coordinates.length / 2 > correctLocations.size()) {
            throw new ApplicationException(ErrorCode.TOO_MANY_COORDINATE_PAIRS);
        }

        try {
            // Validate 6 decimal places for each coordinate
            String decimalPattern = "^-?\\d+\\.\\d{6}$";
            for (String coord : coordinates) {
                if (!coord.matches(decimalPattern)) {
                    throw new ApplicationException(ErrorCode.INVALID_COORDINATE_PRECISION);
                }
            }

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

            // Check for duplicate coordinate pairs
            Set<String> uniqueCoords = new HashSet<>();
            for (double[] coord : userCoordinates) {
                String coordKey = coord[0] + "," + coord[1];
                if (!uniqueCoords.add(coordKey)) {
                    throw new ApplicationException(ErrorCode.DUPLICATE_COORDINATE_PAIR);
                }
            }

            // Track matched correct locations to prevent reuse
            Set<String> matchedLocationIds = new HashSet<>();
            int correctCount = 0;

            for (double[] userCoord : userCoordinates) {
                double userLong = userCoord[0];
                double userLat = userCoord[1];
                boolean isWithinRadius = false;

                for (QuizLocationAnswer location : correctLocations) {
                    // Skip if this location was already matched
                    if (matchedLocationIds.contains(location.getQuizLocationAnswerId())) {
                        continue;
                    }

                    double distance = calculateHaversineDistance(
                            userLat, userLong,
                            location.getLatitude(), location.getLongitude()
                    );

                    double radiusInMeters = location.getRadius() * 1000; // convert km to meters

                    if (distance <= radiusInMeters) {
                        isWithinRadius = true;
                        matchedLocationIds.add(location.getQuizLocationAnswerId()); // Mark as matched
                        break; // Stop checking once a valid location is found
                    }
                }

                if (isWithinRadius) {
                    correctCount++;
                }
            }

            // isCorrect is true only if all correct locations are matched
            boolean isCorrect = correctCount == correctLocations.size();
            // Score based on a proportion of correct matches
            double proportionCorrect = correctLocations.isEmpty() ? 0 : (double) correctCount / correctLocations.size();
            int responseScore = (int) Math.floor(baseScore * proportionCorrect); // Round down
            return new QuizResult(isCorrect, responseScore);
        } catch (NumberFormatException e) {
            throw new ApplicationException(ErrorCode.INVALID_LAT_LNG_FORMAT);
        }
    }

    private double calculateHaversineDistance(double lat1, double long1, double lat2, double long2) {
        final int EARTH_RADIUS = 6371000; // meters
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(long2 - long1);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in meters
    }
}