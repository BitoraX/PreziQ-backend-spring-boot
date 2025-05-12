package com.bitorax.priziq.dto.response.session;

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
public class SessionParticipantHistoryResponse {
    String sessionParticipantId;

    String displayName;
    String displayAvatar;

    List<ActivitySubmissionHistoryResponse> activitySubmissions;

    Integer finalScore;
    Integer finalRanking;
    Integer finalCorrectCount;
    Integer finalIncorrectCount;
}
