package com.bitorax.priziq.service;

import com.bitorax.priziq.domain.Achievement;
import com.bitorax.priziq.dto.request.achievement.AssignAchievementToUserRequest;
import com.bitorax.priziq.dto.request.achievement.CreateAchievementRequest;
import com.bitorax.priziq.dto.request.achievement.UpdateAchievementRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementDetailResponse;
import com.bitorax.priziq.dto.response.achievement.AchievementSummaryResponse;
import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface AchievementService {
    AchievementSummaryResponse createAchievement(CreateAchievementRequest createAchievementRequest);

    AchievementDetailResponse getAchievementById(String achievementId);

    AchievementSummaryResponse updateAchievementById(String achievementId, UpdateAchievementRequest updateAchievementRequest);

    void deleteAchievementById(String achievementId);

    PaginationResponse getAllAchievementsWithQuery(Specification<Achievement> spec, Pageable pageable);

    PaginationResponse getMyAchievements(Specification<Achievement> spec, Pageable pageable);

    AchievementUpdateResponse assignAchievementsToUser(AssignAchievementToUserRequest request);
}
