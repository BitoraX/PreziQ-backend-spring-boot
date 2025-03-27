package com.bitorax.priziq.service;


import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;

public interface ActivityService {
    ActivityResponse createActivity(CreateActivityRequest createActivityRequest);
}
