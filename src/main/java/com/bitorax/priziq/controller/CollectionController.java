package com.bitorax.priziq.controller;

import com.bitorax.priziq.dto.request.collection.CreateCollectionRequest;
import com.bitorax.priziq.dto.response.collection.CollectionResponse;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.permission.PermissionResponse;
import com.bitorax.priziq.service.CollectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
                .statusCode(HttpStatus.CREATED.value())
                .message("Tạo mới bộ sưu tập thành công")
                .data(this.collectionService.createCollection(createCollectionRequest))
                .path(servletRequest.getRequestURI())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<CollectionResponse> getCollectionById(@PathVariable("id") String collectionId, HttpServletRequest servletRequest) {
        return ApiResponse.<CollectionResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Lấy thông tin một bộ sưu tập thành công")
                .data(this.collectionService.getCollectionById(collectionId))
                .path(servletRequest.getRequestURI())
                .build();
    }
}
