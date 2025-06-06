package com.bitorax.priziq.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CollectionBackgroundMusic {
    DOODLE_DASH("Doodle Dash", "https://preziq-spring-uploader.s3.amazonaws.com/sounds/system/Doodle-Dash---Collection-Background-Music-1da87859-d717-4cd7-95a6-5c1b21956c02.mp3"),
    DETECTIVE("Detective", "https://preziq-spring-uploader.s3.amazonaws.com/sounds/system/Detective---Collection-Background-Music-199d7a43-0a61-4be7-82c1-beead3e5af73.mp3"),
    BOUNCY_BEANS("Bouncy Beans", "https://preziq-spring-uploader.s3.amazonaws.com/sounds/system/Bouncy-Beans---Collection-Background-Music-c7875eed-9f7e-46a3-be6e-e7f1a86c0393.mp3"),
    QUIRKY_CARTOON("Quirky Cartoon", "https://preziq-spring-uploader.s3.amazonaws.com/sounds/system/Quirky-Cartoon---Collection-Background-Music-a61248eb-ee08-4e5c-9e3c-237f2a4d8649.mp3"),
    FUNNY_ADVENTURE("Funny Adventure", "https://preziq-spring-uploader.s3.amazonaws.com/sounds/system/Funny-Adventure---Collection-Background-Music-a5b22b19-a829-4a0c-a846-6ad99fc0e2d6.mp3"),
    SILLY_KIDS("Silly Kids", "https://preziq-spring-uploader.s3.amazonaws.com/sounds/system/Silly-Kids---Collection-Background-Music-d1ca52d7-6d2e-45b6-91be-7f672072b079.mp3"),
    STITCH("Stitch", "https://preziq-spring-uploader.s3.ap-southeast-1.amazonaws.com/sounds/system/Stitch+-+Collection+Background+Music.mp3"),
    WHO_ATE_THE_COOKIE("Who Ate The Cookie?", "https://preziq-spring-uploader.s3.amazonaws.com/sounds/system/Who-ate-the-cookie---Collection-Background-Music-aef7f6c9-8ba1-4be3-a198-8c21769ef617.mp3"),

    ;

    String name;
    String fileUrl;

    public static List<Map<String, String>> getAllBackgroundMusic() {
        return Arrays.stream(values())
                .map(music -> Map.of(
                        "name", music.getName(),
                        "fileUrl", music.getFileUrl()
                ))
                .collect(Collectors.toList());
    }
}
