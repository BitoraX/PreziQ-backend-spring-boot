package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.UpdateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.UpdateQuizRequest;
import com.bitorax.priziq.dto.request.activity.slide.CreateSlideElementRequest;
import com.bitorax.priziq.dto.request.activity.slide.UpdateSlideElementRequest;
import com.bitorax.priziq.dto.request.activity.slide.UpdateSlideRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideElementResponse;
import com.bitorax.priziq.dto.response.activity.slide.SlideResponse;

public interface ActivityService {
    ActivityResponse createActivity(CreateActivityRequest createActivityRequest);

    QuizResponse updateQuiz(String activityId, UpdateQuizRequest updateQuizRequest);

    void deleteActivity(String activityId);

    ActivityResponse updateActivity(String activityId, UpdateActivityRequest updateActivityRequest);

    SlideResponse updateSlide(String slideId, UpdateSlideRequest updateSlideRequest);

    SlideElementResponse addSlideElement(String slideId, CreateSlideElementRequest createSlideElementRequest);

    SlideElementResponse updateSlideElement(String slideId, String elementId, UpdateSlideElementRequest updateSlideElementRequest);

    void deleteSlideElement(String slideId, String elementId);
}
