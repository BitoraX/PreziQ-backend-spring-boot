package com.bitorax.priziq.domain.activity.slide;

import com.bitorax.priziq.constant.SlideElementType;
import com.bitorax.priziq.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "slide_elements")
public class SlideElement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String slideElementId;

    @ManyToOne
    @JoinColumn(name = "slide_id", nullable = false)
    @JsonIgnore
    Slide slide;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    SlideElementType slideElementType;

    @Column(nullable = false)
    @Builder.Default
    BigDecimal positionX = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    BigDecimal positionY = BigDecimal.ZERO;

    BigDecimal width;
    BigDecimal height;

    @Column(nullable = false)
    @Builder.Default
    BigDecimal rotation = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    Integer layerOrder = 0;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(columnDefinition = "TEXT")
    String sourceUrl;

    String entryAnimation;

    @Builder.Default
    BigDecimal entryAnimationDuration = BigDecimal.ONE;

    @Builder.Default
    BigDecimal entryAnimationDelay = BigDecimal.ZERO;

    String exitAnimation;

    @Builder.Default
    BigDecimal exitAnimationDuration = BigDecimal.ONE;

    @Builder.Default
    BigDecimal exitAnimationDelay = BigDecimal.ZERO;
}
