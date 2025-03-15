package com.bitorax.priziq.controller;

import com.bitorax.priziq.dto.request.file.*;
import com.bitorax.priziq.dto.response.common.ApiResponse;
import com.bitorax.priziq.dto.response.file.MultipleFileResponse;
import com.bitorax.priziq.dto.response.file.SingleFileResponse;
import com.bitorax.priziq.service.S3FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/storage/aws-s3")
public class S3FileStorageController {
    S3FileStorageService s3FileStorageService;

    @PostMapping("/upload/single")
    public ApiResponse<SingleFileResponse> uploadSingleFile(@Valid SingleUploadFileRequest singleUploadFileRequest,
            HttpServletRequest servletRequest) {
        String folderName = singleUploadFileRequest.getFolderName();
        MultipartFile file = singleUploadFileRequest.getFile();

        String fileUrl = this.s3FileStorageService.uploadSingleFile(file, folderName);
        SingleFileResponse responseDto = new SingleFileResponse(singleUploadFileRequest.getFile().getOriginalFilename(),
                fileUrl);

        return ApiResponse.<SingleFileResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Tải lên một file thành công")
                .data(responseDto)
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PostMapping("/upload/multiple")
    public ApiResponse<MultipleFileResponse> uploadMultipleFiles(
            @Valid MultipleUploadFileRequest multipleUploadFileRequest, HttpServletRequest servletRequest) {
        String folderName = multipleUploadFileRequest.getFolderName();
        List<MultipartFile> files = multipleUploadFileRequest.getFiles();

        List<String> fileUrls = this.s3FileStorageService.uploadMultipleFiles(files, folderName);

        List<SingleFileResponse> fileResponses = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            fileResponses.add(new SingleFileResponse(files.get(i).getOriginalFilename(), fileUrls.get(i)));
        }

        MultipleFileResponse multipleDto = new MultipleFileResponse(fileResponses);

        return ApiResponse.<MultipleFileResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Tải lên nhiều file thành công")
                .data(multipleDto)
                .path(servletRequest.getRequestURI())
                .build();
    }

    @DeleteMapping("/delete/single")
    public ApiResponse<String> deleteSingleFile(@RequestParam("filePath") String filePath,
            HttpServletRequest servletRequest) {
        this.s3FileStorageService.deleteSingleFile(filePath);

        return ApiResponse.<String>builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .message("Xóa một file thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }

    @DeleteMapping("/delete/multiple")
    public ApiResponse<String> deleteMultipleFiles(@RequestBody MultipleDeleteFileRequest multipleDeleteFileRequest,
            HttpServletRequest servletRequest) {
        this.s3FileStorageService.deleteMultipleFiles(multipleDeleteFileRequest.getFilePaths());

        return ApiResponse.<String>builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .message("Xóa nhiều file thành công")
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PutMapping("/move/single")
    public ApiResponse<String> moveSingleFile(@Valid SingleMoveFileRequest singleMoveFileRequest,
            HttpServletRequest servletRequest) {
        String sourceKey = singleMoveFileRequest.getSourceKey();
        String destinationFolder = singleMoveFileRequest.getDestinationFolder();
        this.s3FileStorageService.moveSingleFile(sourceKey, destinationFolder);

        return ApiResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Di chuyển file thành công")
                .data("Đã di chuyển file từ thư mục " + sourceKey + " sang thư mục " + destinationFolder)
                .path(servletRequest.getRequestURI())
                .build();
    }

    @PutMapping("/move/multiple")
    public ApiResponse<String> moveMultipleFiles(@RequestBody MultipleMoveFileRequest requestDto,
            HttpServletRequest servletRequest) {
        this.s3FileStorageService.moveMultipleFiles(requestDto.getSourceKeys(), requestDto.getDestinationFolder());

        return ApiResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Di chuyển nhiều file thành công")
                .data("Các file đã di chuyển tới thư mục: " + requestDto.getDestinationFolder())
                .path(servletRequest.getRequestURI())
                .build();
    }
}
