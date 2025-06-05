package com.bitorax.priziq.configuration;

import com.bitorax.priziq.constant.SessionStatus;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.domain.session.Session;
import com.bitorax.priziq.repository.SessionParticipantRepository;
import com.bitorax.priziq.repository.SessionRepository;
import com.bitorax.priziq.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduledTasks {

    SessionRepository sessionRepository;
    SessionParticipantRepository sessionParticipantRepository;
    UserRepository userRepository;

    private static final long PENDING_SESSION_TIMEOUT_HOURS = 24;
    private static final long STARTED_SESSION_TIMEOUT_HOURS = 7;
    private static final long UNVERIFIED_USER_TIMEOUT_DAYS = 7;

    @Scheduled(cron = "0 0 */3 * * *")
    @Transactional
    public void cleanupStaleSessions() {
        try {
            Instant now = Instant.now();

            // Fetch PENDING sessions older than 24 hours and STARTED sessions older than 7 hours
            List<Session> pendingSessions = sessionRepository.findBySessionStatusAndStartTimeBefore(
                    SessionStatus.PENDING, now.minus(PENDING_SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS));

            List<Session> startedSessions = sessionRepository.findBySessionStatusAndStartTimeBefore(
                    SessionStatus.STARTED, now.minus(STARTED_SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS));

            // Sessions to delete (PENDING)
            List<Session> sessionsToDelete = new ArrayList<>(pendingSessions);

            // Sessions to end (STARTED)
            List<Session> sessionsToEnd = new ArrayList<>();
            for (Session session : startedSessions) {
                if (session.getStartTime() != null) {
                    session.setSessionStatus(SessionStatus.ENDED);
                    session.setEndTime(now);
                    sessionsToEnd.add(session);
                } else {
                    log.warn("Skipping STARTED session {} with null startTime", session.getSessionCode());
                }
            }

            // Batch delete PENDING sessions and their participants
            if (!sessionsToDelete.isEmpty()) {
                List<String> sessionIds = sessionsToDelete.stream()
                        .map(Session::getSessionId)
                        .collect(Collectors.toList());
                sessionParticipantRepository.deleteBySession_SessionIdIn(sessionIds);
                sessionRepository.deleteAllInBatch(sessionsToDelete);
            }

            // Batch update STARTED sessions
            if (!sessionsToEnd.isEmpty()) {
                sessionRepository.saveAll(sessionsToEnd);
            }

        } catch (Exception e) {
            log.error("Failed to cleanup sessions: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupUnverifiedUsers() {
        try {
            List<User> unverifiedUsers = userRepository.findByIsVerifiedFalse();

            for (User user : unverifiedUsers) {
                Instant now = Instant.now();
                Instant createdAt = user.getCreatedAt();

                if (createdAt != null && now.isAfter(createdAt.plus(UNVERIFIED_USER_TIMEOUT_DAYS, ChronoUnit.DAYS))) {
                    user.getRoles().clear();
                    userRepository.delete(user);
                }
            }
        } catch (Exception e) {
            log.error("Failed to cleanup unverified users: {}", e.getMessage());
        }
    }
}