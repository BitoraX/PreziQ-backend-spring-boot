package com.bitorax.priziq.dto.response.session;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionParticipantHistoryResponse {
    String displayName;
    String displayAvatar;

    Integer finalScore;
    Integer finalRanking;
    Integer finalCorrectCount;
    Integer finalIncorrectCount;
}