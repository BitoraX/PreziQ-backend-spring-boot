package com.bitorax.priziq.dto.response.activity;

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
public class ActivitySummaryResponse extends AuditResponse {
    String activityId;
    String activityType;
    String title;
    String description;
    Boolean isPublished;
    Integer orderIndex;
    String backgroundColor;
    String backgroundImage;
    String customBackgroundMusic;
    String conversionWarning;
}
