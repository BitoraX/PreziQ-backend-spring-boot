package com.bitorax.priziq.dto.response.session;

import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
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
public class SessionEndResultResponse {
    SessionSummaryResponse sessionSummary;
    List<AchievementUpdateResponse> achievementUpdates;
}
