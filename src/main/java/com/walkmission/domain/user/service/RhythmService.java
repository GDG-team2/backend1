package com.walkmission.domain.user.service;

import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.domain.user.entity.UserMissionPreference;
import com.walkmission.domain.user.entity.UserProfile;
import com.walkmission.domain.user.repository.UserMissionPreferenceRepository;
import com.walkmission.domain.user.repository.UserProfileRepository;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import com.walkmission.global.util.TimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 주간 리듬: 매일 연속 대신 "이번 주 N번" 목표를 채운 주가 몇 주 이어졌는지를 센다.
 * 하루 빠졌다고 끊기지 않고, 지난주 목표를 놓쳤을 때만 끊긴다.
 */
@Service
public class RhythmService {
    private final UserProfileRepository userProfileRepository;
    private final UserMissionPreferenceRepository preferenceRepository;
    private final MissionRecordRepository missionRecordRepository;

    public RhythmService(UserProfileRepository userProfileRepository,
                         UserMissionPreferenceRepository preferenceRepository,
                         MissionRecordRepository missionRecordRepository) {
        this.userProfileRepository = userProfileRepository;
        this.preferenceRepository = preferenceRepository;
        this.missionRecordRepository = missionRecordRepository;
    }

    /**
     * @param goalJustAchieved 이번 평가에서 이번 주 목표를 막 채웠는지 (완료 화면 축하 표시용)
     */
    public record RhythmResult(int weeklyGoal, int thisWeekCount, boolean goalAchievedThisWeek,
                               int currentWeeks, int bestWeeks, boolean goalJustAchieved) {}

    /** 이번 주 완료 횟수로 목표 달성 여부를 평가해 반영한다. 미션 완료·목표 변경 후에 호출한다. */
    @Transactional
    public RhythmResult evaluate(Long userId) {
        return calculate(userId, true);
    }

    @Transactional(readOnly = true)
    public RhythmResult get(Long userId) {
        return calculate(userId, false);
    }

    private RhythmResult calculate(Long userId, boolean applyAchievement) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        int goal = preferenceRepository.findByUserId(userId)
                .map(UserMissionPreference::getWeeklyGoal)
                .orElse(UserMissionPreference.DEFAULT_WEEKLY_GOAL);

        LocalDate weekStart = TimeUtils.currentWeekStart();
        int thisWeekCount = (int) missionRecordRepository.countByUserIdAndStatusAndCompletedAtGreaterThanEqual(
                userId, MissionStatus.COMPLETED, weekStart.atStartOfDay());

        boolean justAchieved = applyAchievement && thisWeekCount >= goal && profile.achieveWeeklyGoal(weekStart);
        boolean achievedThisWeek = weekStart.equals(profile.getLastRhythmWeekStart());

        return new RhythmResult(goal, thisWeekCount, achievedThisWeek,
                profile.getRhythmWeeksAsOf(weekStart), profile.getBestRhythmWeeks(), justAchieved);
    }
}
