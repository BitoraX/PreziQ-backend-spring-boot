package com.bitorax.priziq.dto.response.achievement;

import com.bitorax.priziq.dto.response.common.AuditResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AchievementSummaryResponse extends AuditResponse {
    String achievementId;
    String name;
    String description;
    String iconUrl;
    Integer requiredPoints;
}
