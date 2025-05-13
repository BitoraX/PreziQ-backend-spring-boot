package com.bitorax.priziq.constant;

import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CollectionTopicType {
    ART,
    LITERATURE,
    ENTERTAINMENT,
    GEOGRAPHY,
    HISTORY,
    LANGUAGES,
    SCIENCE,
    NATURE,
    SPORTS,
    TRIVIA

    ;

    public static void validateCollectionTopicType(String type) {
        boolean isValid = Arrays.stream(values()).anyMatch(topicType -> topicType.name().equalsIgnoreCase(type));
        if (!isValid) {
            throw new ApplicationException(ErrorCode.INVALID_COLLECTION_TOPIC_TYPE);
        }
    }
}
