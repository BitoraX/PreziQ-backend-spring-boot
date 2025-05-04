package com.bitorax.priziq.dto.response.achievement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AchievementUpdateResponse {
    String userId;
    Integer totalPoints;
    List<AchievementSummaryResponse> newAchievements;
}
