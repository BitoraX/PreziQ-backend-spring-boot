package com.bitorax.priziq.dto.response.activity.slide;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SlideResponse {
    String slideId;
    List<SlideElementResponse> slideElements;
    String transitionEffect;
    BigDecimal transitionDuration;
    Integer autoAdvanceSeconds;
}