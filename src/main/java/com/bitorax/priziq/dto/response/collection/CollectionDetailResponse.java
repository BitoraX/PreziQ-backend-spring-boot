package com.bitorax.priziq.dto.response.collection;

import com.bitorax.priziq.dto.response.activity.ActivityDetailResponse;
import com.bitorax.priziq.dto.response.common.AuditResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionDetailResponse extends AuditResponse {
    String collectionId;
    String title;
    String description;
    Boolean isPublished;
    String coverImage;
    String defaultBackgroundMusic;
    String topic;
    List<ActivityDetailResponse> activities;
}
