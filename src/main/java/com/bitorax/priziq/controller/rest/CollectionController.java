package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.constant.CollectionTopicType;
import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.ActivityReorderRequest;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionDetailResponse;
import com.bitorax.priziq.dto.response.collection.CollectionSummaryResponse;
import com.bitorax.priziq.dto.response.collection.ReorderedActivityResponse;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.service.CollectionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/collections")
public class CollectionController {

    CollectionService collectionService;

    @PostMapping
    ApiResponse<CollectionSummaryResponse> createCollection(@RequestBody @Valid CreateCollectionRequest createCollectionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<CollectionSummaryResponse>builder()
                .message("Collection created successfully")
                .data(collectionService.createCollection(createCollectionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{collectionId}")
    ApiResponse<CollectionDetailResponse> getCollectionById(@PathVariable String collectionId, HttpServletRequest servletRequest) {
        return ApiResponse.<CollectionDetailResponse>builder()
                .message("Collection retrieved successfully")
                .data(collectionService.getCollectionById(collectionId))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping
    ApiResponse<PaginationResponse> getAllCollectionWithQuery(@Filter Specification<Collection> spec, Pageable pageable, HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("Collections retrieved successfully with query filters")
                .data(collectionService.getAllCollectionWithQuery(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{collectionId}")
    ApiResponse<CollectionSummaryResponse> updateCollectionById(@RequestBody UpdateCollectionRequest updateCollectionRequest, @PathVariable String collectionId, HttpServletRequest servletRequest) {
        return ApiResponse.<CollectionSummaryResponse>builder()
                .message("Collection updated successfully")
                .data(collectionService.updateCollectionById(collectionId, updateCollectionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/me")
    ApiResponse<PaginationResponse> getMyCollections(@Filter Specification<Collection> spec, Pageable pageable, HttpServletRequest servletRequest) {
        return ApiResponse.<PaginationResponse>builder()
                .message("My collections retrieved successfully")
                .data(collectionService.getMyCollections(spec, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @DeleteMapping("/{collectionId}")
    ApiResponse<Void> deleteCollectionById(@PathVariable String collectionId, HttpServletRequest servletRequest) {
        collectionService.deleteCollectionById(collectionId);
        return ApiResponse.<Void>builder()
                .message("Collection deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PutMapping("/{collectionId}/activities/reorder")
    ApiResponse<List<ReorderedActivityResponse>> reorderActivities(@PathVariable String collectionId, @RequestBody @Valid ActivityReorderRequest activityReorderRequest, HttpServletRequest servletRequest){
        return ApiResponse.<List<ReorderedActivityResponse>>builder()
                .message("Activities reordered successfully")
                .data(collectionService.reorderActivities(collectionId, activityReorderRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/topics")
    ApiResponse<List<String>> getAllCollectionTopics(HttpServletRequest servletRequest){
        return ApiResponse.<List<String>>builder()
                .message("Retrieved the list of collection topics successfully")
                .data(CollectionTopicType.getAllKeys())
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/grouped/topics")
    public ApiResponse<Map<String, List<CollectionSummaryResponse>>> getCollectionsGroupedByTopic(
            @RequestParam(required = false) CollectionTopicType topic,
            @PageableDefault(size = 7) Pageable pageable,
            HttpServletRequest servletRequest) {
        return ApiResponse.<Map<String, List<CollectionSummaryResponse>>>builder()
                .message("Collections grouped by topic retrieved successfully")
                .data(collectionService.getCollectionsGroupedByTopic(topic, pageable))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
