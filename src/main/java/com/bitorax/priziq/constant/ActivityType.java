package com.bitorax.priziq.constant;

import com.bitorax.priziq.dto.response.common.ActivityTypeInfo;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ActivityType {
    QUIZ_BUTTONS("Buttons", "Select one correct answer from multiple choices", "quiz_buttons_icon"),
    QUIZ_CHECKBOXES("Checkboxes", "Select multiple correct answers from a list of options", "quiz_checkboxes_icon"),
    QUIZ_REORDER("Reorder", "Reorder steps or options in the correct sequence.", "quiz_reorder_icon"),
    QUIZ_TYPE_ANSWER("Type Answer", "Enter the correct answer manually.", "quiz_type_answer_icon"),
    QUIZ_TRUE_OR_FALSE("True or False", "Choose between True or False.", "quiz_true_false_icon"),
    QUIZ_LOCATION("Location", "Choose the correct location", "quiz_location_icon"),
    INFO_SLIDE("Info Slide", "Display informational content without interaction.", "info_slide_icon");

    String name;
    String description;
    String icon;

    public static void validateActivityType(String type) {
        boolean isValid = Arrays.stream(values()).anyMatch(activityType -> activityType.name().equalsIgnoreCase(type));
        if (!isValid) {
            throw new ApplicationException(ErrorCode.INVALID_ACTIVITY_TYPE);
        }
    }

    public static List<ActivityTypeInfo> getAllTypes() {
        return Arrays.stream(values())
                .map(type -> ActivityTypeInfo.builder()
                        .key(type.name())
                        .name(type.getName())
                        .description(type.getDescription())
                        .icon(type.getIcon())
                        .build()
                )
                .toList();
    }
}
