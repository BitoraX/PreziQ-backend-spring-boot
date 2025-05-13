package com.bitorax.priziq.constant;

import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CollectionTopicType {
    PUBLISH, // not show in validate and keys

    ART,
    SCIENCE,
    TECHNOLOGY,
    HISTORY,
    LITERATURE,
    ENTERTAINMENT,
    SPORTS,
    GEOGRAPHY,
    HEALTH,
    EDUCATION,
    NATURE,
    CULTURE,
    BUSINESS,
    PHILOSOPHY,
    FOOD,
    TRIVIA

    ;

    public static void validateCollectionTopicType(String type) {
        boolean isValid = Arrays.stream(values())
                .anyMatch(topicType -> topicType.name().equalsIgnoreCase(type) && topicType != CollectionTopicType.PUBLISH);
        if (!isValid) {
            throw new ApplicationException(ErrorCode.INVALID_COLLECTION_TOPIC_TYPE);
        }
    }

    public static List<String> getAllKeys() {
        return Arrays.stream(values())
                .filter(topicType -> topicType != CollectionTopicType.PUBLISH)
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
