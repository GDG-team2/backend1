package com.walkmission.domain.user.service;

import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.domain.reward.repository.BadgeRepository;
import com.walkmission.domain.user.dto.PreferenceResponse;
import com.walkmission.domain.user.dto.PreferenceUpdateRequest;
import com.walkmission.domain.user.dto.UserProfileResponse;
import com.walkmission.domain.user.dto.UserSettingsUpdateRequest;
import com.walkmission.domain.user.dto.UserSettingsUpdateResponse;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.entity.UserMissionPreference;
import com.walkmission.domain.user.entity.UserProfile;
import com.walkmission.domain.user.entity.UserSetting;
import com.walkmission.domain.user.repository.UserMissionPreferenceRepository;
import com.walkmission.domain.user.repository.UserProfileRepository;
import com.walkmission.domain.user.repository.UserRepository;
import com.walkmission.domain.user.repository.UserSettingRepository;
import com.walkmission.global.util.RegionUtils;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSettingRepository userSettingRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final BadgeRepository badgeRepository;
    private final UserMissionPreferenceRepository preferenceRepository;
    private final RhythmService rhythmService;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository, UserSettingRepository userSettingRepository,
                       MissionRecordRepository missionRecordRepository, BadgeRepository badgeRepository,
                       UserMissionPreferenceRepository preferenceRepository,
                       RhythmService rhythmService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSettingRepository = userSettingRepository;
        this.missionRecordRepository = missionRecordRepository;
        this.badgeRepository = badgeRepository;
        this.preferenceRepository = preferenceRepository;
        this.rhythmService = rhythmService;
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        long completedMissions = missionRecordRepository.countByUserIdAndStatus(userId, MissionStatus.COMPLETED);
        RhythmService.RhythmResult rhythm = rhythmService.get(userId);
        UserProfileResponse.BadgeInfo representativeBadge = profile.getRepresentativeBadgeId() == null ? null
                : badgeRepository.findById(profile.getRepresentativeBadgeId())
                        .map(b -> new UserProfileResponse.BadgeInfo(b.getId(), b.getBadgeName(), b.getIconUrl()))
                        .orElse(null);

        return new UserProfileResponse(
            user.getUserUuid(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            new UserProfileResponse.RegionInfo(user.getRegionCode(), RegionUtils.nameOf(user.getRegionCode())),
            new UserProfileResponse.AssetInfo(profile.getCurrentPoint()),
            new UserProfileResponse.RhythmInfo(rhythm.weeklyGoal(), rhythm.thisWeekCount(), rhythm.goalAchievedThisWeek(),
                    rhythm.currentWeeks(), rhythm.bestWeeks()),
            representativeBadge,
            new UserProfileResponse.StatsInfo((int) completedMissions)
        );
    }

    @Transactional
    public UserSettingsUpdateResponse updateSettings(Long userId, UserSettingsUpdateRequest request) {
        UserSetting setting = userSettingRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        setting.update(request);

        return new UserSettingsUpdateResponse(
            setting.getUser().getUserUuid(),
            setting.getAllAlarm(), setting.getStartAlarm(), setting.getMissionAlarm(),
            setting.getInsightAlarm(), setting.getRewardAlarm(),
            setting.getQuietStart(), setting.getQuietEnd(),
            setting.getRankingSetting(), setting.getNameSetting(),
            setting.getPlaceSetting(), setting.getFriendSetting(),
            setting.getUpdatedAt(),
            "설정이 성공적으로 변경되었습니다."
        );
    }

    @Transactional(readOnly = true)
    public PreferenceResponse getPreference(Long userId) {
        return toPreferenceResponse(getPreferenceEntity(userId));
    }

    @Transactional
    public PreferenceResponse updatePreference(Long userId, PreferenceUpdateRequest request) {
        UserMissionPreference preference = getPreferenceEntity(userId);
        preference.update(request.walkTime(), request.moveType(), request.categories(), request.weeklyGoal());
        if (request.weeklyGoal() != null) rhythmService.evaluate(userId); // 목표를 낮춰 이미 채웠다면 바로 반영
        return toPreferenceResponse(preference);
    }

    private UserMissionPreference getPreferenceEntity(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private PreferenceResponse toPreferenceResponse(UserMissionPreference preference) {
        return new PreferenceResponse(
                preference.getWalkTime(),
                preference.getMoveType(),
                preference.getEffectiveCategories().stream().sorted().toList(),
                preference.getWeeklyGoal());
    }
}
