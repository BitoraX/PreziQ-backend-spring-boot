package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.UpdateQuizRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizAnswerResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "activityType", source = "activityType")
    Activity createActivityRequestToActivity(CreateActivityRequest createActivityRequest);

    ActivityResponse activityToResponse(Activity activity);

    @Mapping(target = "quizId", ignore = true)
    @Mapping(target = "activity", ignore = true)
    void updateQuizFromRequest(UpdateQuizRequest request, @MappingTarget Quiz quiz);

    @Mapping(target = "answers", source = "quizAnswers")
    QuizResponse quizToResponse(Quiz quiz);

    QuizAnswerResponse quizAnswerToResponse(QuizAnswer quizAnswer);
}
