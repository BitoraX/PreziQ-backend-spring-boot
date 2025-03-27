package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.Activity;
import com.bitorax.priziq.domain.ActivityType;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.activity.CreateActivityRequest;
import com.bitorax.priziq.dto.response.activity.ActivityResponse;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityMapper;
import com.bitorax.priziq.repository.ActivityRepository;
import com.bitorax.priziq.repository.ActivityTypeRepository;
import com.bitorax.priziq.repository.CollectionRepository;
import com.bitorax.priziq.service.ActivityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityServiceImp implements ActivityService {
    ActivityRepository activityRepository;
    CollectionRepository collectionRepository;
    ActivityTypeRepository activityTypeRepository;
    ActivityMapper activityMapper;

    @Override
    public ActivityResponse createActivity(CreateActivityRequest createActivityRequest){
        Collection currentCollection = collectionRepository
                .findById(createActivityRequest.getCollectionId())
                .orElseThrow(() -> new AppException(ErrorCode.COLLECTION_NOT_FOUND));

        ActivityType currentActivityType = activityTypeRepository
                .findById(createActivityRequest.getActivityTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_TYPE_NOT_FOUND));

        Activity activity = activityMapper.createActivityRequestToActivity(createActivityRequest);
        activity.setCollection(currentCollection);
        activity.setActivityType(currentActivityType);

        return activityMapper.activityToResponse(activityRepository.save(activity));
    }
}
