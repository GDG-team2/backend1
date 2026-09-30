package com.walkmission.domain.reward.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walkmission.domain.mission.entity.MissionStatus;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.domain.reward.dto.BadgeListResponse;
import com.walkmission.domain.reward.dto.PointHistoryResponse;
import com.walkmission.domain.reward.entity.*;
import com.walkmission.domain.reward.repository.BadgeRepository;
import com.walkmission.domain.reward.repository.PointHistoryRepository;
import com.walkmission.domain.reward.repository.UserBadgeRepository;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.entity.UserProfile;
import com.walkmission.domain.user.repository.UserProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RewardService {
    private static final int MAX_PAGE_SIZE = 100;

    private final PointHistoryRepository pointHistoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final ObjectMapper objectMapper;

    public RewardService(PointHistoryRepository pointHistoryRepository, UserProfileRepository userProfileRepository,
                         BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository,
                         MissionRecordRepository missionRecordRepository, ObjectMapper objectMapper) {
        this.pointHistoryRepository = pointHistoryRepository;
        this.userProfileRepository = userProfileRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.missionRecordRepository = missionRecordRepository;
        this.objectMapper = objectMapper;
    }

    /** 포인트를 적립하고 적립 후 보유 포인트를 반환한다. */
    @Transactional
    public int earnPoint(User user, int amount, String description) {
        UserProfile profile = getProfile(user.getId());
        profile.addPoint(amount);
        pointHistoryRepository.save(new PointHistory(user, amount, PointType.EARN, description));
        return profile.getCurrentPoint();
    }

    /** 조건을 새로 만족한 배지를 지급하고 반환한다. 대표 배지가 없으면 첫 배지를 대표로 지정한다. */
    @Transactional
    public List<Badge> awardBadges(User user) {
        Long userId = user.getId();
        UserProfile profile = getProfile(userId);
        List<Long> ownedBadgeIds = userBadgeRepository.findByUserId(userId).stream()
                .map(ub -> ub.getBadge().getId())
                .toList();

        long missionCount = missionRecordRepository.countByUserIdAndStatus(userId, MissionStatus.COMPLETED);
        long placeCount = missionRecordRepository.countDistinctPlaces(userId, MissionStatus.COMPLETED);
        int bestRhythmWeeks = profile.getBestRhythmWeeks();

        List<Badge> newBadges = new ArrayList<>();
        for (Badge badge : badgeRepository.findByCodeIsNotNullOrderByIdAsc()) {
            if (ownedBadgeIds.contains(badge.getId())) continue;

            BadgeCondition condition = parseCondition(badge);
            long progress = switch (condition.type()) {
                case MISSION_COUNT -> missionCount;
                case PLACE_COUNT -> placeCount;
                case RHYTHM_WEEKS -> bestRhythmWeeks;
            };
            if (progress >= condition.threshold()) {
                userBadgeRepository.save(new UserBadge(user, badge));
                newBadges.add(badge);
            }
        }

        if (profile.getRepresentativeBadgeId() == null && !newBadges.isEmpty()) {
            profile.changeRepresentativeBadge(newBadges.get(0).getId());
        }
        return newBadges;
    }

    @Transactional(readOnly = true)
    public PointHistoryResponse getPointHistory(Long userId, int page, int size) {
        UserProfile profile = getProfile(userId);
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, MAX_PAGE_SIZE)));
        Page<PointHistory> result = pointHistoryRepository.findByUserIdOrderByIdDesc(userId, pageRequest);

        return new PointHistoryResponse(
                profile.getCurrentPoint(),
                result.getContent().stream()
                        .map(h -> new PointHistoryResponse.PointHistoryEntry(
                                h.getId(), h.getType().name(), h.getAmount(), h.getDescription(), h.getCreatedAt()))
                        .toList(),
                new PointHistoryResponse.PaginationInfo(
                        result.getNumber(), result.getSize(), result.getTotalPages(),
                        result.getTotalElements(), result.hasNext())
        );
    }

    @Transactional(readOnly = true)
    public BadgeListResponse getBadges(Long userId) {
        UserProfile profile = getProfile(userId);
        Map<Long, UserBadge> owned = userBadgeRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(ub -> ub.getBadge().getId(), Function.identity(), (a, b) -> a));

        List<BadgeListResponse.BadgeDetail> details = badgeRepository.findByCodeIsNotNullOrderByIdAsc().stream()
                .map(badge -> {
                    UserBadge userBadge = owned.get(badge.getId());
                    return new BadgeListResponse.BadgeDetail(
                            badge.getId(), badge.getBadgeName(), badge.getDescription(), badge.getIconUrl(),
                            userBadge != null,
                            Objects.equals(badge.getId(), profile.getRepresentativeBadgeId()),
                            userBadge != null ? userBadge.getCreatedAt() : null);
                })
                .toList();

        return new BadgeListResponse(
                new BadgeListResponse.BadgeSummary(details.size(),
                        (int) details.stream().filter(BadgeListResponse.BadgeDetail::isAcquired).count()),
                details
        );
    }

    private BadgeCondition parseCondition(Badge badge) {
        try {
            return objectMapper.readValue(badge.getBadgeCondition(), BadgeCondition.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid badge condition for badge " + badge.getId(), e);
        }
    }

    private UserProfile getProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
