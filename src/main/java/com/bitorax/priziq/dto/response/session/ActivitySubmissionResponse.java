package com.bitorax.priziq.dto.response.session;

import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.common.AuditResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
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
public class ActivitySubmissionResponse extends AuditResponse {
    String activitySubmissionId;
    SessionResponse session;
    UserSecureResponse user;
    ActivityResponse activity;
    String answerContent;
    Boolean isCorrect;
    Integer score;
}
