package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.constant.HttpMethodType;
import com.bitorax.priziq.dto.request.permission.CreateModuleRequest;
import com.bitorax.priziq.dto.request.permission.CreatePermissionRequest;
import com.bitorax.priziq.dto.request.permission.UpdatePermissionRequest;
import com.bitorax.priziq.dto.response.common.MetaResponse;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.dto.response.permission.PermissionResponse;
import com.bitorax.priziq.domain.Permission;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.PermissionMapper;
import com.bitorax.priziq.repository.PermissionRepository;
import com.bitorax.priziq.service.PermissionService;
import com.bitorax.priziq.utils.PermissionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImp implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    PermissionUtils permissionUtils;

    @Override
    public List<PermissionResponse> createModuleForPermissions(CreateModuleRequest createModuleRequest) {
        List<String> permissionIds = createModuleRequest.getPermissionIds();

        String moduleName = createModuleRequest.getModuleName().toUpperCase();
        boolean moduleExists = this.permissionRepository.existsByModule(moduleName);
        if (moduleExists)
            throw new AppException(ErrorCode.PERMISSION_MODULE_NAME_EXISTED);

        this.permissionUtils.checkDuplicatePermissionIds(permissionIds);
        List<Permission> permissions = this.permissionUtils.validatePermissionsExist(permissionIds);
        this.permissionUtils.validatePermissionsBelongToModule(permissions, moduleName);

        permissions.forEach(permission -> permission.setModule(moduleName));
        this.permissionRepository.saveAll(permissions);

        return this.permissionMapper.permissionsToPermissionResponseList(permissions);
    }

    @Override
    public void deleteModuleByName(String moduleName) {
        boolean moduleExists = this.permissionRepository.existsByModule(moduleName.toUpperCase());
        if (!moduleExists)
            throw new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND);

        List<Permission> permissionsInModule = this.permissionRepository.findByModule(moduleName)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
        permissionsInModule.forEach(permission -> permission.setModule(null));

        this.permissionRepository.saveAll(permissionsInModule);
    }

    @Override
    public List<String> getAllModules() {
        return this.permissionRepository.findDistinctModules();
    }

    @Override
    public PermissionResponse createPermission(CreatePermissionRequest createPermissionRequest) {
        this.permissionUtils.validateUniquePermissionOnCreate(createPermissionRequest.getName(),
                createPermissionRequest.getApiPath(), createPermissionRequest.getHttpMethod());
        HttpMethodType.validateHttpMethod(createPermissionRequest.getHttpMethod());

        String moduleName = createPermissionRequest.getModule();
        if (moduleName != null && !moduleName.isEmpty()) {
            moduleName = moduleName.toUpperCase();
            boolean moduleExists = this.permissionRepository.existsByModule(moduleName);
            if (!moduleExists)
                throw new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND);
        }

        Permission permission = this.permissionMapper.createPermissionRequestToPermission(createPermissionRequest);
        permission.setModule(moduleName); // can null

        return this.permissionMapper.permissionToResponse(this.permissionRepository.save(permission));
    }

    @Override
    public PermissionResponse getPermissionById(String permissionId) {
        return this.permissionMapper.permissionToResponse(this.permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND)));
    }

    @Override
    public PaginationResponse getAllPermissionWithQuery(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> permissionPage = this.permissionRepository.findAll(spec, pageable);
        return PaginationResponse.builder()
                .meta(MetaResponse.builder()
                        .currentPage(pageable.getPageNumber() + 1) // base-index = 0
                        .pageSize(pageable.getPageSize())
                        .totalPages(permissionPage.getTotalPages())
                        .totalElements(permissionPage.getTotalElements())
                        .hasNext(permissionPage.hasNext())
                        .hasPrevious(permissionPage.hasPrevious())
                        .build())
                .content(this.permissionMapper.permissionsToPermissionResponseList(permissionPage.getContent()))
                .build();
    }

    @Override
    public PermissionResponse updatePermissionById(String permissionId, UpdatePermissionRequest updatePermissionRequest) {
        Permission currentPermission = this.permissionRepository.findById(permissionId).orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        String newHttpMethod = updatePermissionRequest.getHttpMethod();
        if (newHttpMethod != null) {
            HttpMethodType.validateHttpMethod(newHttpMethod);
        }

        String newName = updatePermissionRequest.getName();
        String newApiPath = updatePermissionRequest.getApiPath();
        this.permissionUtils.validateUniquePermissionOnUpdate(permissionId, newName, newApiPath, newHttpMethod);

        this.permissionMapper.updatePermissionRequestToPermission(currentPermission, updatePermissionRequest);

        // Logic handle field module
        String newModule = updatePermissionRequest.getModule();
        if (newModule != null) {
            newModule = newModule.toUpperCase();
            if (!this.permissionRepository.existsByModule(newModule))
                throw new AppException(ErrorCode.PERMISSION_MODULE_NOT_FOUND);
            currentPermission.setModule(newModule);
        } else {
            currentPermission.setModule(null);
        }

        return this.permissionMapper.permissionToResponse(this.permissionRepository.save(currentPermission));
    }

    @Override
    public void deletePermissionById(String permissionId) {
        Permission currentPermission = this.permissionRepository.findById(permissionId).orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));

        currentPermission.getRoles().forEach(role -> role.getPermissions().remove(currentPermission));
        currentPermission.getRoles().clear();

        this.permissionRepository.delete(currentPermission);
    }
}
