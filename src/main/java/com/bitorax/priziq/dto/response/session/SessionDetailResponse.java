package com.bitorax.priziq.dto.response.session;

import com.bitorax.priziq.dto.response.collection.CollectionSummaryResponse;
import com.bitorax.priziq.dto.response.common.AuditResponse;
import com.bitorax.priziq.dto.response.user.UserSecureResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionDetailResponse extends AuditResponse {
    String sessionId;
    String sessionCode;
    String joinSessionQrUrl;
    String sessionStatus;

    CollectionSummaryResponse collection;
    UserSecureResponse hostUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant endTime;

}
