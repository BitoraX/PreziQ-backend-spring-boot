package com.bitorax.priziq.controller.rest;

import com.bitorax.priziq.domain.Collection;
import com.bitorax.priziq.dto.request.collection.ActivityReorderRequest;
import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.request.collection.UpdateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;
import com.bitorax.priziq.dto.response.collection.ReorderedActivityResponse;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.service.CollectionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bitorax.priziq.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/v1/collections")
public class CollectionController {

    CollectionService collectionService;

    @PostMapping
    ApiResponse<CollectionResponse> createCollection(@RequestBody @Valid CreateCollectionRequest createCollectionRequest, HttpServletRequest servletRequest) {
        return ApiResponse.<CollectionResponse>builder()
                .message("Collection created successfully")
                .data(collectionService.createCollection(createCollectionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<CollectionResponse> updateCollectionById(@RequestBody UpdateCollectionRequest updateCollectionRequest, @PathVariable("id") String collectionId, HttpServletRequest servletRequest) {
        return ApiResponse.<CollectionResponse>builder()
                .message("Collection updated successfully")
                .data(collectionService.updateCollectionById(collectionId, updateCollectionRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<CollectionResponse> getCollectionById(@PathVariable("id") String collectionId, HttpServletRequest servletRequest) {
        return ApiResponse.<CollectionResponse>builder()
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

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteCollectionById(@PathVariable("id") String collectionId, HttpServletRequest servletRequest) {
        collectionService.deleteCollectionById(collectionId);
        return ApiResponse.<Void>builder()
                .message("Collection deleted successfully")
                .meta(buildMetaInfo(servletRequest))
                .build();
    }

    @PutMapping("/{id}/activities/reorder")
    ApiResponse<List<ReorderedActivityResponse>> reorderActivities(@PathVariable("id") String collectionId, @RequestBody @Valid ActivityReorderRequest activityReorderRequest, HttpServletRequest servletRequest){
        return ApiResponse.<List<ReorderedActivityResponse>>builder()
                .message("Activities reordered successfully")
                .data(collectionService.reorderActivities(collectionId, activityReorderRequest))
                .meta(buildMetaInfo(servletRequest))
                .build();
    }
}
