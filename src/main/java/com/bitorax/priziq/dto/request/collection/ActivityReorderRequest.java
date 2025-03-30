package com.bitorax.priziq.dto.request.collection;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ActivityReorderRequest {
    @NotEmpty(message = "ORDER_ACTIVITY_IDS_NOT_EMPTY")
    List<String> orderedActivityIds;
}
