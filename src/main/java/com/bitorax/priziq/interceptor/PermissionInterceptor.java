package com.bitorax.priziq.interceptor;

import com.bitorax.priziq.domain.Permission;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionInterceptor implements HandlerInterceptor {
    UserRepository userRepository;
    SecurityUtils securityUtils;

    @Override
    @Transactional // fix lazy loading (FetchType = LAZY)
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {
        String apiPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String httpMethod = request.getMethod();

        // Authorization (check allowed permissions)
        User userAuthenticated = this.securityUtils.getAuthenticatedUser();
        Set<Permission> allPermissions = userAuthenticated.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());

        boolean isAuthorized = allPermissions.stream().anyMatch(permission -> permission.getApiPath().equals(apiPath)
                && permission.getHttpMethod().equalsIgnoreCase(httpMethod));
        if (!isAuthorized)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        return true;
    }
}
