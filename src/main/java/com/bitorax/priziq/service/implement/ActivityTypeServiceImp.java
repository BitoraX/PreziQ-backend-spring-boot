package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.ActivityType;
import com.bitorax.priziq.dto.request.activity_types.CreateActivityTypeRequest;
import com.bitorax.priziq.dto.request.activity_types.UpdateActivityTypeRequest;
import com.bitorax.priziq.dto.response.activity_types.ActivityTypeResponse;
import com.bitorax.priziq.dto.response.common.PaginationMeta;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.ActivityTypeMapper;
import com.bitorax.priziq.repository.ActivityTypeRepository;
import com.bitorax.priziq.service.ActivityTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityTypeServiceImp implements ActivityTypeService {
    ActivityTypeRepository activityTypeRepository;
    ActivityTypeMapper activityTypeMapper;

    @Override
    public ActivityTypeResponse createActivityType(CreateActivityTypeRequest createActivityTypeRequest){
        if(activityTypeRepository.existsByName(createActivityTypeRequest.getName())){
            throw new AppException(ErrorCode.ACTIVITY_TYPE_NAME_EXISTED);
        }

        ActivityType activityType = activityTypeMapper.createActivityTypeRequestToActivityType(createActivityTypeRequest);
        return activityTypeMapper.activityTypeToResponse(activityTypeRepository.save(activityType));
    }

    @Override
    public ActivityTypeResponse getActivityTypeById(String activityTypeId){
        return activityTypeMapper.activityTypeToResponse(activityTypeRepository.findById(activityTypeId).orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_TYPE_NOT_FOUND)));
    }

    @Override
    public PaginationResponse getAllActivityTypeWithQuery(Specification<ActivityType> spec, Pageable pageable) {
        Page<ActivityType> activityTypePage = this.activityTypeRepository.findAll(spec, pageable);
        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1) // base-index = 0
                        .pageSize(pageable.getPageSize())
                        .totalPages(activityTypePage.getTotalPages())
                        .totalElements(activityTypePage.getTotalElements())
                        .hasNext(activityTypePage.hasNext())
                        .hasPrevious(activityTypePage.hasPrevious())
                        .build())
                .content(this.activityTypeMapper.activityTypeToActivityTypeResponseList(activityTypePage.getContent()))
                .build();
    }

    @Override
    public ActivityTypeResponse updateActivityTypeById(String activityTypeId, UpdateActivityTypeRequest updateActivityTypeRequest){
        ActivityType currentActivityType = this.activityTypeRepository.findById(activityTypeId).orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_TYPE_NOT_FOUND    ));
        if(activityTypeRepository.existsByName(updateActivityTypeRequest.getName())){
            throw new AppException(ErrorCode.ACTIVITY_TYPE_NAME_EXISTED);
        }

        this.activityTypeMapper.updateActivityTypeRequestToActivityType(currentActivityType, updateActivityTypeRequest);
        return this.activityTypeMapper.activityTypeToResponse(activityTypeRepository.save(currentActivityType));
    }

    @Override
    public void deleteActivityTypeById(String activityTypeId){
        ActivityType currentActivityType = this.activityTypeRepository.findById(activityTypeId).orElseThrow(() -> new AppException(ErrorCode.ACTIVITY_TYPE_NOT_FOUND    ));
        this.activityTypeRepository.delete(currentActivityType);
    }
}
