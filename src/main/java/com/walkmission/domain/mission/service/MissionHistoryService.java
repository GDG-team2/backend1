package com.walkmission.domain.mission.service;

import com.walkmission.domain.mission.dto.*;
import com.walkmission.domain.mission.entity.*;
import com.walkmission.domain.mission.repository.HistoryStats;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import com.walkmission.global.util.TimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/** 완료한 미션 기록 조회: 활동 기록(M02), 기록 상세(D06), 내 지도(06) */
@Service
@Transactional(readOnly = true)
public class MissionHistoryService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int RECENT_RECORDS = 5;
    private static final LocalDate ALL_FROM = LocalDate.of(2000, 1, 1);
    private static final LocalDate ALL_TO = LocalDate.of(2100, 1, 1);

    private final MissionRecordRepository missionRecordRepository;

    public MissionHistoryService(MissionRecordRepository missionRecordRepository) {
        this.missionRecordRepository = missionRecordRepository;
    }

    /** 조회 구간 [from, to) */
    private record Range(PeriodInfo info, LocalDateTime from, LocalDateTime to) {}

    public MissionHistoryResponse getHistory(Long userId, HistoryPeriod period, YearMonth yearMonth,
                                             PlaceCategory category, boolean freeOnly, int page, int size) {
        Range range = resolve(period, yearMonth);
        Set<PlaceCategory> categories = filterCategories(category, freeOnly);
        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        if (categories.isEmpty()) { // 조건에 맞는 범주가 없으면 빈 결과 (빈 IN 절을 보내지 않음)
            return new MissionHistoryResponse(range.info(),
                    new MissionHistoryResponse.Summary(0, 0, null, 0, 0), List.of(),
                    new MissionHistoryResponse.Pagination(Math.max(page, 0), pageSize, 0, 0L, false));
        }

        HistoryStats stats = missionRecordRepository.aggregateCompleted(userId, range.from(), range.to(), categories);
        Page<MissionRecord> result = missionRecordRepository.findCompletedHistory(userId, range.from(), range.to(),
                categories, PageRequest.of(Math.max(page, 0), pageSize));

        return new MissionHistoryResponse(
                range.info(),
                new MissionHistoryResponse.Summary(
                        stats.completedCount().intValue(),
                        stats.totalDurationMinutes().intValue(),
                        stats.averageSatisfaction() == null ? null
                                : Math.round(stats.averageSatisfaction() * 10) / 10.0,
                        PlaceRecommender.roundTripRouteMeters(stats.totalStraightDistanceMeters().intValue()),
                        stats.newPlaceCount().intValue()),
                result.getContent().stream().map(this::toItem).toList(),
                new MissionHistoryResponse.Pagination(result.getNumber(), result.getSize(), result.getTotalPages(),
                        result.getTotalElements(), result.hasNext()));
    }

    public MissionMapResponse getMap(Long userId, HistoryPeriod period, YearMonth yearMonth) {
        Range range = resolve(period, yearMonth);
        Set<PlaceCategory> all = EnumSet.allOf(PlaceCategory.class);

        HistoryStats stats = missionRecordRepository.aggregateCompleted(userId, range.from(), range.to(), all);
        List<MissionMapResponse.VisitedPlace> places = missionRecordRepository
                .findVisitedPlaces(userId, range.from(), range.to()).stream()
                .map(p -> new MissionMapResponse.VisitedPlace(p.placeId(), p.name(), p.category(),
                        p.latitude(), p.longitude(), p.visitCount().intValue(), p.lastVisitedAt()))
                .toList();
        List<MissionRecordItem> recent = missionRecordRepository.findCompletedHistory(userId, range.from(), range.to(),
                        all, PageRequest.of(0, RECENT_RECORDS))
                .getContent().stream().map(this::toItem).toList();

        return new MissionMapResponse(
                range.info(),
                new MissionMapResponse.Stats(stats.completedCount().intValue(), stats.newPlaceCount().intValue(),
                        PlaceRecommender.roundTripRouteMeters(stats.totalStraightDistanceMeters().intValue())),
                places,
                recent);
    }

    public MissionDetailResponse getDetail(Long userId, Long missionId) {
        MissionRecord m = missionRecordRepository.findByIdAndUserId(missionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));
        Place place = m.getPlace();
        PlaceCategory category = m.getPlaceCategory();

        Integer before = m.getBeforeSurvey();
        Integer after = m.getAfterSurvey();

        return new MissionDetailResponse(
                m.getId(),
                m.getStatus().name(),
                m.getMissionTitle(),
                m.getRecommendReason(),
                new MissionRecommendResponse.PlaceInfo(place.getId(), place.getKakaoPlaceId(), place.getName(),
                        place.getCategory(), place.getRoadAddress(), place.getLatitude(), place.getLongitude(),
                        place.getPlaceUrl()),
                category,
                m.getMoveType(),
                m.getIsNewPlace(),
                m.getDistanceMeters() != null ? PlaceRecommender.roundTripRouteMeters(m.getDistanceMeters()) : null,
                m.getDurationMinutes(),
                category != null ? category.getEstimatedCost() : null,
                m.getStepCount(),
                new MissionDetailResponse.MoodChange(m.getBeforeMood(), before, after,
                        before != null && after != null ? after - before : null),
                timeline(m),
                m.getStatus() == MissionStatus.ABORTED && (m.getAbortReason() != null || m.getMovedDistanceMeters() != null)
                        ? new MissionDetailResponse.AbortInfo(m.getAbortReason(), m.getMovedDistanceMeters())
                        : null);
    }

    private List<MissionDetailResponse.TimelineEvent> timeline(MissionRecord m) {
        List<MissionDetailResponse.TimelineEvent> events = new ArrayList<>();
        if (m.getScheduledAt() != null) {
            events.add(new MissionDetailResponse.TimelineEvent("SCHEDULED", m.getScheduledAt(), "출발 약속"));
        }
        if (m.getStartedAt() != null) {
            events.add(new MissionDetailResponse.TimelineEvent("STARTED", m.getStartedAt(), startNote(m)));
        }
        if (m.getArrivedAt() != null) {
            events.add(new MissionDetailResponse.TimelineEvent("ARRIVED", m.getArrivedAt(), "목적지 근처에서 머물러 도착 인증"));
        }
        if (m.getCompletedAt() != null) {
            String note = m.getAfterSurvey() != null ? "만족도 " + m.getAfterSurvey() + "/5로 저장" : "완료";
            events.add(new MissionDetailResponse.TimelineEvent("COMPLETED", m.getCompletedAt(), note));
        }
        if (m.getAbortedAt() != null && m.getStatus() == MissionStatus.ABORTED) {
            String note = m.getAbortReason() != null ? m.getAbortReason().getLabel()
                    : m.getRejectReason() != null ? "다른 추천으로 바꿈 (" + m.getRejectReason().getLabel() + ")"
                    : "중단";
            events.add(new MissionDetailResponse.TimelineEvent("ABORTED", m.getAbortedAt(), note));
        }
        events.sort(Comparator.comparing(MissionDetailResponse.TimelineEvent::at));
        return events;
    }

    private String startNote(MissionRecord m) {
        if (m.getScheduledAt() == null) return "출발";
        long diff = Duration.between(m.getScheduledAt(), m.getStartedAt()).toMinutes();
        if (diff < 0) return "약속보다 " + (-diff) + "분 빠르게 시작";
        if (diff > 0) return "약속보다 " + diff + "분 늦게 시작";
        return "약속한 시간에 시작";
    }

    private MissionRecordItem toItem(MissionRecord m) {
        PlaceCategory category = m.getPlaceCategory();
        return new MissionRecordItem(
                m.getId(),
                m.getMissionTitle(),
                m.getPlaceNameSnapshot(),
                category,
                m.getCompletedAt(),
                m.getDurationMinutes(),
                m.getDistanceMeters() != null ? PlaceRecommender.roundTripRouteMeters(m.getDistanceMeters()) : null,
                m.getAfterSurvey(),
                m.getIsNewPlace(),
                category != null ? category.getEstimatedCost() : null);
    }

    /** 범주 필터: 지정 범주만, 무료만(예상 비용 0원 범주), 둘 다 없으면 전체. 비어 있을 수 있다(예: 카페 + 무료). */
    private Set<PlaceCategory> filterCategories(PlaceCategory category, boolean freeOnly) {
        Set<PlaceCategory> result = category != null ? EnumSet.of(category) : EnumSet.allOf(PlaceCategory.class);
        if (freeOnly) result.removeIf(c -> c.getEstimatedCost() > 0);
        return result;
    }

    private Range resolve(HistoryPeriod period, YearMonth yearMonth) {
        HistoryPeriod p = period != null ? period : HistoryPeriod.MONTH;
        return switch (p) {
            case WEEK -> {
                LocalDate start = TimeUtils.currentWeekStart();
                yield new Range(new PeriodInfo(p, start, start.plusDays(6)),
                        start.atStartOfDay(), start.plusWeeks(1).atStartOfDay());
            }
            case MONTH -> {
                YearMonth ym = yearMonth != null ? yearMonth : YearMonth.from(TimeUtils.today());
                yield new Range(new PeriodInfo(p, ym.atDay(1), ym.atEndOfMonth()),
                        ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
            }
            case ALL -> new Range(new PeriodInfo(p, null, null), ALL_FROM.atStartOfDay(), ALL_TO.atStartOfDay());
        };
    }
}
