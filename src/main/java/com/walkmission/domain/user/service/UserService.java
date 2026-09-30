package com.walkmission.domain.user.service;

import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.domain.user.dto.UserProfileResponse;
import com.walkmission.domain.user.dto.UserSettingsUpdateRequest;
import com.walkmission.domain.user.dto.UserSettingsUpdateResponse;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.entity.UserProfile;
import com.walkmission.domain.user.entity.UserSetting;
import com.walkmission.domain.user.repository.UserProfileRepository;
import com.walkmission.domain.user.repository.UserRepository;
import com.walkmission.domain.user.repository.UserSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSettingRepository userSettingRepository;
    private final MissionRecordRepository missionRecordRepository;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository, UserSettingRepository userSettingRepository,
                       MissionRecordRepository missionRecordRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSettingRepository = userSettingRepository;
        this.missionRecordRepository = missionRecordRepository;
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        long completedMissions = missionRecordRepository.countByUserIdAndStatus(userId, MissionStatus.COMPLETED);

        return new UserProfileResponse(
            user.getUserUuid(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            new UserProfileResponse.RegionInfo(user.getRegionCode(), "지역 정보 없음"),
            new UserProfileResponse.AssetInfo(profile.getCurrentPoint()),
            new UserProfileResponse.StreakInfo(profile.getStreakNow(), profile.getStreakRecord()),
            new UserProfileResponse.BadgeInfo(1L, "걷기 초보", "url"),
            new UserProfileResponse.StatsInfo((int) completedMissions)
        );
    }

    @Transactional
    public UserSettingsUpdateResponse updateSettings(Long userId, UserSettingsUpdateRequest request) {
        UserSetting setting = userSettingRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found"));

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
}
