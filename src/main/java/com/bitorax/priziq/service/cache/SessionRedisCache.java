package com.bitorax.priziq.service.cache;

import com.bitorax.priziq.dto.cache.*;
import com.bitorax.priziq.utils.CacheUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionRedisCache {

    CacheUtils cacheUtils;
    static String SESSION_CACHE_PREFIX = "session:";
    static String COLLECTION_KEY_SUFFIX = ":collection";
    static String ACTIVITIES_KEY_SUFFIX = ":activities";
    static String PARTICIPANTS_KEY_SUFFIX = ":participants";
    static String SUBMISSIONS_KEY_SUFFIX = ":submissions";

    public void cacheSession(String sessionId, SessionCacheDTO sessionCacheDTO) {
        cacheUtils.cacheHash(SESSION_CACHE_PREFIX + sessionId, sessionCacheDTO, 7200); // TTL 2 hours
    }

    public SessionCacheDTO getCachedSession(String sessionId) {
        return cacheUtils.getCachedHash(SESSION_CACHE_PREFIX + sessionId, SessionCacheDTO.class);
    }

    public void cacheCollection(String sessionId, CollectionCacheDTO collectionCacheDTO) {
        cacheUtils.cacheHash(SESSION_CACHE_PREFIX + sessionId + COLLECTION_KEY_SUFFIX, collectionCacheDTO, 7200);
    }

    public CollectionCacheDTO getCachedCollection(String sessionId) {
        return cacheUtils.getCachedHash(SESSION_CACHE_PREFIX + sessionId + COLLECTION_KEY_SUFFIX, CollectionCacheDTO.class);
    }

    public void cacheActivities(String sessionId, List<ActivityCacheDTO> activities) {
        cacheUtils.cacheList(SESSION_CACHE_PREFIX + sessionId + ACTIVITIES_KEY_SUFFIX, activities, 7200);
    }

    public List<ActivityCacheDTO> getCachedActivities(String sessionId) {
        return cacheUtils.getCachedList(SESSION_CACHE_PREFIX + sessionId + ACTIVITIES_KEY_SUFFIX, ActivityCacheDTO.class);
    }

    public void cacheParticipants(String sessionId, List<ParticipantCacheDTO> participants) {
        cacheUtils.cacheList(SESSION_CACHE_PREFIX + sessionId + PARTICIPANTS_KEY_SUFFIX, participants, 7200);
    }

    public List<ParticipantCacheDTO> getCachedParticipants(String sessionId) {
        return cacheUtils.getCachedList(SESSION_CACHE_PREFIX + sessionId + PARTICIPANTS_KEY_SUFFIX, ParticipantCacheDTO.class);
    }

    public void cacheSubmissions(String sessionId, List<SubmissionCacheDTO> submissions) {
        cacheUtils.cacheList(SESSION_CACHE_PREFIX + sessionId + SUBMISSIONS_KEY_SUFFIX, submissions, 7200);
    }

    public List<SubmissionCacheDTO> getCachedSubmissions(String sessionId) {
        return cacheUtils.getCachedList(SESSION_CACHE_PREFIX + sessionId + SUBMISSIONS_KEY_SUFFIX, SubmissionCacheDTO.class);
    }

    public void updateParticipantScoreAndRanking(String sessionId, String participantId, int score, int ranking) {
        List<ParticipantCacheDTO> participants = getCachedParticipants(sessionId);
        participants.stream()
                .filter(p -> p.getSessionParticipantId().equals(participantId))
                .findFirst()
                .ifPresent(p -> {
                    p.setRealtimeScore(score);
                    p.setRealtimeRanking(ranking);
                });
        cacheUtils.cacheList(SESSION_CACHE_PREFIX + sessionId + PARTICIPANTS_KEY_SUFFIX, participants, 7200);
    }

    public void removeCachedSession(String sessionId) {
        cacheUtils.deleteCache(
                SESSION_CACHE_PREFIX + sessionId,
                SESSION_CACHE_PREFIX + sessionId + COLLECTION_KEY_SUFFIX,
                SESSION_CACHE_PREFIX + sessionId + ACTIVITIES_KEY_SUFFIX,
                SESSION_CACHE_PREFIX + sessionId + PARTICIPANTS_KEY_SUFFIX,
                SESSION_CACHE_PREFIX + sessionId + SUBMISSIONS_KEY_SUFFIX
        );
    }
}