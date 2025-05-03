package com.bitorax.priziq.domain.session;

import com.bitorax.priziq.domain.BaseEntity;
import com.bitorax.priziq.domain.activity.Activity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "activity_submissions")
public class ActivitySubmission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String activitySubmissionId;

    @ManyToOne
    @JoinColumn(name = "session_participant_id", nullable = false)
    SessionParticipant sessionParticipant;

    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    Activity activity;

    @Column(columnDefinition = "TEXT")
    String answerContent;

    @Column(nullable = false)
    Boolean isCorrect;

    @Column(nullable = false)
    Integer responseScore;
}