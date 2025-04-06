package com.bitorax.priziq.dto.response.activity.slide;

import com.bitorax.priziq.constant.SlideElementType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SlideElementResponse {
    String slideElementId;
    SlideElementType slideElementType;
    BigDecimal positionX;
    BigDecimal positionY;
    BigDecimal width;
    BigDecimal height;
    BigDecimal rotation;
    Integer zIndex;
    String content;
    String sourceUrl;
    String entryAnimation;
    BigDecimal entryAnimationDuration;
    BigDecimal entryAnimationDelay;
    String exitAnimation;
    BigDecimal exitAnimationDuration;
    BigDecimal exitAnimationDelay;
}