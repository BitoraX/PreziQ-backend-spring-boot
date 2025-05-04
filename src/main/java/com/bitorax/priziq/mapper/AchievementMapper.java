package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.Achievement;
import com.bitorax.priziq.dto.request.achievement.CreateAchievementRequest;
import com.bitorax.priziq.dto.request.achievement.UpdateAchievementRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementDetailResponse;
import com.bitorax.priziq.dto.response.achievement.AchievementSummaryResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AchievementMapper {
    AchievementDetailResponse achievementToDetailResponse(Achievement achievement);

    AchievementSummaryResponse achievementToSummaryResponse(Achievement achievement);

    Achievement createAchievementRequestToAchievement(CreateAchievementRequest createAchievementRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAchievementRequestToAchievement(@MappingTarget Achievement achievement, UpdateAchievementRequest updateAchievementRequest);

    List<AchievementDetailResponse> achievementsToDetailResponseList(List<Achievement> achievements);

    List<AchievementSummaryResponse> achievementsToSummaryResponseList(List<Achievement> achievements);
}
