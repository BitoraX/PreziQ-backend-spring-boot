package com.bitorax.priziq.service;

import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.request.activity.quiz.UpdateQuizRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.dto.response.activity.quiz.QuizResponse;

public interface ActivityService {
    ActivityResponse createActivity(CreateActivityRequest createActivityRequest);

    QuizResponse updateQuiz(String activityId, UpdateQuizRequest updateQuizRequest);

    void deleteActivity(String activityId);
}
