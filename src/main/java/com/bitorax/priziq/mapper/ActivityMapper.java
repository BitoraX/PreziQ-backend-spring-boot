package com.bitorax.priziq.mapper;

import com.bitorax.priziq.domain.activity.Activity;
import com.bitorax.priziq.domain.activity.quiz.Quiz;
import com.bitorax.priziq.domain.activity.quiz.QuizAnswer;
import com.bitorax.priziq.domain.activity.quiz.QuizLocationAnswer;
import com.bitorax.priziq.domain.activity.slide.Slide;
import com.bitorax.priziq.domain.activity.slide.SlideElement;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.UpdateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.UpdateQuizRequest;
import com.bitorax.priziq.dto.request.activity.slide.CreateSlideElementRequest;
import com.bitorax.priziq.dto.request.activity.slide.UpdateSlideElementRequest;
import com.bitorax.priziq.dto.request.activity.slide.UpdateSlideRequest;
import com.bitorax.priziq.dto.response.activity.ActivityDetailResponse;
import com.bitorax.priziq.dto.response.activity.ActivitySummaryResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizAnswerResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizLocationAnswerResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideElementResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "activityType", source = "activityType")
    Activity createActivityRequestToActivity(CreateActivityRequest createActivityRequest);

    ActivityDetailResponse activityToDetailResponse(Activity activity);

    ActivitySummaryResponse activityToSummaryResponse(Activity activity);

    @Mapping(target = "quizId", ignore = true)
    @Mapping(target = "activity", ignore = true)
    void updateQuizFromRequest(UpdateQuizRequest request, @MappingTarget Quiz quiz);

    @Mapping(target = "quizAnswers", source = "quizAnswers")
    QuizResponse quizToResponse(Quiz quiz);

    QuizAnswerResponse quizAnswerToResponse(QuizAnswer quizAnswer);

    QuizLocationAnswerResponse quizLocationAnswerToResponse(QuizLocationAnswer quizLocationAnswer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateActivityFromRequest(UpdateActivityRequest updateActivityRequest, @MappingTarget Activity activity);

    SlideElement createSlideElementRequestToSlideElement(CreateSlideElementRequest request);

    SlideElementResponse slideElementToResponse(SlideElement slideElement);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSlideElementFromRequest(UpdateSlideElementRequest request, @MappingTarget SlideElement slideElement);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSlideFromRequest(UpdateSlideRequest request, @MappingTarget Slide slide);

    SlideResponse slideToResponse(Slide slide);
}
