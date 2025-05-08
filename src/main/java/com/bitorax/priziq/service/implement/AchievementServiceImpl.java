package com.bitorax.priziq.service.implement;

import com.bitorax.priziq.domain.Achievement;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.dto.request.achievement.AssignAchievementToUserRequest;
import com.bitorax.priziq.dto.request.achievement.CreateAchievementRequest;
import com.bitorax.priziq.dto.request.achievement.UpdateAchievementRequest;
import com.bitorax.priziq.dto.response.achievement.AchievementDetailResponse;
import com.bitorax.priziq.dto.response.achievement.AchievementSummaryResponse;
import com.bitorax.priziq.dto.response.achievement.AchievementUpdateResponse;
import com.bitorax.priziq.dto.response.common.PaginationMeta;
import com.bitorax.priziq.dto.response.common.PaginationResponse;
import com.bitorax.priziq.exception.ApplicationException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.AchievementMapper;
import com.bitorax.priziq.repository.AchievementRepository;
import com.bitorax.priziq.repository.UserRepository;
import com.bitorax.priziq.service.AchievementService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AchievementServiceImpl implements AchievementService {
    AchievementRepository achievementRepository;
    UserRepository userRepository;
    AchievementMapper achievementMapper;

    @Override
    public AchievementSummaryResponse createAchievement(CreateAchievementRequest createAchievementRequest) {
        // Validate and normalize name
        String normalizedName = validateAndNormalizeAchievementName(createAchievementRequest.getName(), null);

        Achievement achievement = achievementMapper.createAchievementRequestToAchievement(createAchievementRequest);
        achievement.setName(normalizedName); // Apply name uppercase

        return achievementMapper.achievementToSummaryResponse(achievementRepository.save(achievement));
    }

    @Override
    public AchievementSummaryResponse updateAchievementById(String achievementId, UpdateAchievementRequest updateAchievementRequest) {
        Achievement currentAchievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ACHIEVEMENT_NOT_FOUND));

        // Validate and normalize name
        if (updateAchievementRequest.getName() != null) {
            String normalizedName = validateAndNormalizeAchievementName(updateAchievementRequest.getName(), achievementId);
            updateAchievementRequest.setName(normalizedName); // Update name uppercase
        }

        achievementMapper.updateAchievementRequestToAchievement(currentAchievement, updateAchievementRequest);
        return achievementMapper.achievementToSummaryResponse(achievementRepository.save(currentAchievement));
    }

    @Override
    public AchievementDetailResponse getAchievementById(String achievementId) {
        return achievementMapper.achievementToDetailResponse(achievementRepository
                .findById(achievementId).orElseThrow(() -> new ApplicationException(ErrorCode.ACHIEVEMENT_NOT_FOUND)));
    }

    @Override
    public PaginationResponse getAllAchievementsWithQuery(Specification<Achievement> spec, Pageable pageable) {
        Page<Achievement> achievementPage = achievementRepository.findAll(spec, pageable);
        return PaginationResponse.builder()
                .meta(PaginationMeta.builder()
                        .currentPage(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalPages(achievementPage.getTotalPages())
                        .totalElements(achievementPage.getTotalElements())
                        .hasNext(achievementPage.hasNext())
                        .hasPrevious(achievementPage.hasPrevious())
                        .build())
                .content(achievementMapper.achievementsToSummaryResponseList(achievementPage.getContent()))
                .build();
    }

    @Override
    public void deleteAchievementById(String achievementId) {
        Achievement currentAchievement = achievementRepository
                .findById(achievementId).orElseThrow(() -> new ApplicationException(ErrorCode.ACHIEVEMENT_NOT_FOUND));
        achievementRepository.delete(currentAchievement);
    }

    @Override
    public AchievementUpdateResponse assignAchievementsToUser(AssignAchievementToUserRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        List<Achievement> newAchievements = achievementRepository.findByRequiredPointsLessThanEqual(user.getTotalPoints());
        List<Achievement> currentAchievements = user.getAchievements();

        List<Achievement> achievementsToAdd = newAchievements.stream()
                .filter(achievement -> !currentAchievements.contains(achievement))
                .toList();

        if (!achievementsToAdd.isEmpty()) {
            for (Achievement achievement : achievementsToAdd) {
                currentAchievements.add(achievement);
                List<User> achievementUsers = achievement.getUsers();
                if (achievementUsers == null) {
                    achievementUsers = new ArrayList<>();
                    achievement.setUsers(achievementUsers);
                }
                if (!achievementUsers.contains(user)) {
                    achievementUsers.add(user);
                }
            }
            userRepository.save(user);
            achievementRepository.saveAll(achievementsToAdd);
        }

        return AchievementUpdateResponse.builder()
                .userId(user.getUserId())
                .totalPoints(user.getTotalPoints())
                .newAchievements(achievementsToAdd.stream()
                        .map(achievement -> AchievementSummaryResponse.builder()
                                .achievementId(achievement.getAchievementId())
                                .name(achievement.getName())
                                .description(achievement.getDescription())
                                .iconUrl(achievement.getIconUrl())
                                .requiredPoints(achievement.getRequiredPoints())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private String validateAndNormalizeAchievementName(String name, String excludeAchievementId) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApplicationException(ErrorCode.ACHIEVEMENT_NAME_NOT_BLANK);
        }

        String normalizedName = name.trim().toUpperCase();

        // Validate name uniqueness, ignoring the current achievementId in case of an update
        if (achievementRepository.existsByName(normalizedName) &&
                (excludeAchievementId == null || !achievementRepository.findById(excludeAchievementId)
                        .map(a -> a.getName().equals(normalizedName))
                        .orElse(false))) {
            throw new ApplicationException(ErrorCode.ACHIEVEMENT_NAME_DUPLICATED);
        }

        return normalizedName;
    }
}
