package com.bitorax.priziq.configuration;

import com.bitorax.priziq.interceptor.PermissionInterceptor;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    UserRepository userRepository;
    SecurityUtils securityUtils;

    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor(userRepository, securityUtils);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/api/v1/auth/register", "/api/v1/auth/verify-active-account", "/api/v1/auth/login",
                "/api/v1/auth/refresh", "/api/v1/auth/resend-verify", "/api/v1/auth/forgot-password",
                "/api/v1/auth/reset-password", "/api/v1/users/{id}", "/api/v1/collections/{id}, /api/v1/collections",
                "/api/v1/activities/types", "/api/v1/achievements/{achievementId}", "/api/v1/achievements", "/ws/**"
        };

        registry.addInterceptor(getPermissionInterceptor()).excludePathPatterns(whiteList);
    }
}
