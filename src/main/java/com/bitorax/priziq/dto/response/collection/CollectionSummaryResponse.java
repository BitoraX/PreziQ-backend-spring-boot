package com.bitorax.priziq.dto.response.collection;

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
public class CollectionSummaryResponse extends AuditResponse {
    String collectionId;
    String title;
    String description;
    Boolean isPublished;
    String coverImage;
    String defaultBackgroundMusic;
    String topic;
    Integer totalActivities;
}
