package com.walkmission.domain.ranking.service;

import com.walkmission.domain.mission.entity.MissionRecord;
import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.domain.ranking.dto.RegionRankingResponse;
import com.walkmission.domain.ranking.entity.Ranking;
import com.walkmission.domain.ranking.entity.RankingHistory;
import com.walkmission.domain.ranking.repository.RankingHistoryRepository;
import com.walkmission.domain.ranking.repository.RankingRepository;
import com.walkmission.domain.reward.RewardPolicy;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.entity.UserSetting;
import com.walkmission.domain.user.repository.UserRepository;
import com.walkmission.domain.user.repository.UserSettingRepository;
import com.walkmission.global.util.RegionUtils;
import com.walkmission.global.util.TimeUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RankingService {
    private static final int MAX_LEADERBOARD_SIZE = 100;

    private final RankingRepository rankingRepository;
    private final RankingHistoryRepository rankingHistoryRepository;
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final MissionRecordRepository missionRecordRepository;

    public RankingService(RankingRepository rankingRepository, RankingHistoryRepository rankingHistoryRepository,
                          UserRepository userRepository, UserSettingRepository userSettingRepository,
                          MissionRecordRepository missionRecordRepository) {
        this.rankingRepository = rankingRepository;
        this.rankingHistoryRepository = rankingHistoryRepository;
        this.userRepository = userRepository;
        this.userSettingRepository = userSettingRepository;
        this.missionRecordRepository = missionRecordRepository;
    }

    /**
     * @param earnedScore  이번 미션으로 얻은 점수 (보너스 포함, 주간 한도를 넘었으면 0)
     * @param bonusScore   earnedScore 중 다양성 보너스
     */
    public record ScoreResult(boolean isParticipant, int earnedScore, int bonusScore, int weeklyScore,
                              int scoredMissionCount, int maxScoredMissions) {}

    /**
     * 미션 완료 점수를 이번 주 랭킹에 더한다. 주 N회까지만 반영하고, 그 주에 처음 해본 범주면 보너스를 준다.
     * 랭킹 비공개여도 점수는 쌓고 리더보드에서만 숨긴다.
     */
    @Transactional
    public ScoreResult addMissionScore(User user, MissionRecord mission) {
        LocalDate weekStart = TimeUtils.currentWeekStart();
        closeFinishedWeeks(weekStart);

        Ranking ranking = rankingRepository.findByUserId(user.getId())
                .orElseGet(() -> rankingRepository.save(new Ranking(user, weekStart)));
        boolean participant = isRankingPublic(user.getId());
        int max = RewardPolicy.MAX_SCORED_MISSIONS_PER_WEEK;

        if (ranking.getScoredMissionCount() >= max) {
            return new ScoreResult(participant, 0, 0, ranking.getUserScore(), ranking.getScoredMissionCount(), max);
        }

        boolean newCategoryThisWeek = mission.getPlaceCategory() != null
                && !missionRecordRepository.existsByUserIdAndStatusAndPlaceCategoryAndCompletedAtGreaterThanEqualAndIdNot(
                        user.getId(), MissionStatus.COMPLETED, mission.getPlaceCategory(),
                        weekStart.atStartOfDay(), mission.getId());
        int bonus = newCategoryThisWeek ? RewardPolicy.NEW_CATEGORY_BONUS_SCORE : 0;
        ranking.addMissionScore(RewardPolicy.MISSION_COMPLETE_RANKING_SCORE, bonus);

        return new ScoreResult(participant, RewardPolicy.MISSION_COMPLETE_RANKING_SCORE + bonus, bonus,
                ranking.getUserScore(), ranking.getScoredMissionCount(), max);
    }

    /** 매주 월요일 0시(KST)에 지난주 랭킹을 기록으로 옮기고 점수를 초기화한다. */
    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void closeFinishedWeeks() {
        closeFinishedWeeks(TimeUtils.weekStart(TimeUtils.today()));
    }

    @Transactional(readOnly = true)
    public RegionRankingResponse getMyRegionRanking(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String districtCode = RegionUtils.districtOf(user.getRegionCode());
        LocalDate weekStart = TimeUtils.weekStart(TimeUtils.today());
        int size = Math.max(1, Math.min(limit, MAX_LEADERBOARD_SIZE));

        List<Ranking> top = rankingRepository.findLeaderboard(districtCode, weekStart, PageRequest.of(0, size));
        List<RegionRankingResponse.RankingEntry> leaderboard = new ArrayList<>();
        int rank = 0;
        Integer prevScore = null;
        for (int i = 0; i < top.size(); i++) {
            Ranking r = top.get(i);
            if (!r.getUserScore().equals(prevScore)) {
                rank = i + 1; // 동점자는 같은 순위 (1, 2, 2, 4 ...)
                prevScore = r.getUserScore();
            }
            User u = r.getUser();
            leaderboard.add(new RegionRankingResponse.RankingEntry(
                    null, u.getUserUuid(), u.getDisplayName(), u.getProfileImageUrl(), rank, r.getUserScore(),
                    r.getScoredMissionCount(), r.getBonusScore()));
        }

        Ranking mine = rankingRepository.findByUserId(userId)
                .filter(r -> weekStart.equals(r.getWeekStartDate()))
                .orElse(null);
        int myScore = mine != null ? mine.getUserScore() : 0;
        boolean participating = isRankingPublic(userId);
        Integer myRank = participating && myScore > 0
                ? (int) rankingRepository.countHigherScores(districtCode, weekStart, myScore) + 1
                : null;

        return new RegionRankingResponse(
                new RegionRankingResponse.RegionInfo(districtCode, RegionUtils.districtNameOf(districtCode)),
                new RegionRankingResponse.WeekPeriodInfo(weekStart, weekStart.plusDays(6)),
                new RegionRankingResponse.RankingEntry(
                        participating, user.getUserUuid(), user.getDisplayName(), user.getProfileImageUrl(), myRank, myScore,
                        mine != null ? mine.getScoredMissionCount() : 0, mine != null ? mine.getBonusScore() : 0),
                leaderboard
        );
    }

    private void closeFinishedWeeks(LocalDate currentWeekStart) {
        if (!rankingRepository.existsByWeekStartDateBefore(currentWeekStart)) return;

        List<Ranking> finished = rankingRepository.findAllBeforeWeek(currentWeekStart);
        List<Ranking> scored = finished.stream().filter(r -> r.getUserScore() > 0).toList();
        Set<Long> publicUserIds = scored.isEmpty() ? Set.of() : new HashSet<>(
                userSettingRepository.findRankingPublicUserIds(scored.stream().map(r -> r.getUser().getId()).toList()));

        Map<String, List<Ranking>> groups = scored.stream()
                .collect(Collectors.groupingBy(r -> RegionUtils.districtOf(r.getUser().getRegionCode()) + "|" + r.getWeekStartDate()));

        for (List<Ranking> group : groups.values()) {
            group.sort(Comparator.comparing(Ranking::getUserScore).reversed());
            // 리더보드와 동일하게 랭킹 공개 사용자끼리만 순위를 매기고, 비공개 사용자는 점수만 기록한다.
            int position = 0;
            int rank = 0;
            Integer prevScore = null;
            for (Ranking r : group) {
                Integer historyRank = null;
                if (publicUserIds.contains(r.getUser().getId())) {
                    position++;
                    if (!r.getUserScore().equals(prevScore)) {
                        rank = position;
                        prevScore = r.getUserScore();
                    }
                    historyRank = rank;
                }
                rankingHistoryRepository.save(new RankingHistory(
                        r.getUser(), historyRank, r.getUserScore(), r.getWeekStartDate(), r.getWeekStartDate().plusDays(6)));
            }
        }

        finished.forEach(r -> r.resetForWeek(currentWeekStart));
    }

    private boolean isRankingPublic(Long userId) {
        return userSettingRepository.findByUserId(userId)
                .map(UserSetting::getRankingSetting)
                .orElse(false);
    }
}
