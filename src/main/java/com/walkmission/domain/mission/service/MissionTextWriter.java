package com.walkmission.domain.mission.service;

import com.walkmission.domain.mission.entity.*;
import com.walkmission.domain.mission.repository.MissionRecordRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 미션 제목과 "왜 이 미션인가요?" 문구를 만든다. 규칙 기반이며 최대 두 문장. */
@Component
public class MissionTextWriter {
    private static final int SAME_CATEGORY_RUN = 3;
    private static final int MIN_SURVEYS_FOR_INSIGHT = 2;
    private static final double SATISFACTION_GAP = 0.3;

    private final MissionRecordRepository missionRecordRepository;

    public MissionTextWriter(MissionRecordRepository missionRecordRepository) {
        this.missionRecordRepository = missionRecordRepository;
    }

    public String title(String placeName, PlaceCategory category) {
        return switch (category) {
            case WALK -> placeName + " 한 바퀴";
            case CAFE -> placeName + "에서 쉬어가기";
            case SIGHTSEEING -> placeName + " 구경하기";
            case EXHIBITION -> placeName + " 둘러보기";
            case FOOD -> placeName + "에서 한 끼";
        };
    }

    public String reason(Long userId, PlaceCategory category, boolean isNewPlace, Mood mood,
                         RejectReason rejectReason, Budget budget) {
        List<String> sentences = new ArrayList<>();

        // 1. 최근 같은 범주만 연속으로 갔다면
        List<MissionRecord> recent = missionRecordRepository.findTop3ByUserIdAndStatusOrderByCompletedAtDesc(
                userId, MissionStatus.COMPLETED);
        if (recent.size() == SAME_CATEGORY_RUN) {
            PlaceCategory last = recent.get(0).getPlaceCategory();
            boolean sameRun = last != null && last != category
                    && recent.stream().allMatch(m -> m.getPlaceCategory() == last);
            if (sameRun) sentences.add("최근 " + last.getLabel() + " 방문이 " + SAME_CATEGORY_RUN + "회 연속이었어요.");
        }

        // 2. 이 범주를 다녀온 뒤 만족도가 평소보다 높았다면
        if (missionRecordRepository.countSurveyed(userId, category) >= MIN_SURVEYS_FOR_INSIGHT) {
            Double categoryAvg = missionRecordRepository.averageAfterSurvey(userId, category);
            Double overallAvg = missionRecordRepository.averageAfterSurvey(userId);
            if (categoryAvg != null && overallAvg != null && categoryAvg - overallAvg >= SATISFACTION_GAP) {
                sentences.add(category.getLabel() + " 외출 뒤 만족도가 더 높았어요.");
            }
        }

        // 3. 이번 요청의 조건
        if (rejectReason != null) {
            sentences.add(switch (rejectReason) {
                case TOO_FAR -> "조금 더 가까운 곳으로 바꿨어요.";
                case DISLIKE_ACTIVITY -> "다른 종류의 장소로 골랐어요.";
                case ALREADY_VISITED -> "아직 안 가본 곳으로 골랐어요.";
                case NO_SPENDING -> "돈 들이지 않고 다녀올 수 있는 곳이에요.";
            });
        } else if (mood != null) {
            sentences.add(switch (mood) {
                case TIRED -> "지친 날이라 가까운 곳으로 골랐어요.";
                case ENERGETIC -> "기운 있는 날이라 조금 더 멀리 골랐어요.";
                case BORED, GLOOMY -> "기분 전환이 되도록 새로운 곳을 골랐어요.";
            });
        }

        if (isNewPlace && sentences.size() < 2) sentences.add("처음 가보는 곳이에요.");
        if (budget == Budget.FREE && category.getEstimatedCost() == 0 && sentences.size() < 2) {
            sentences.add("비용 없이 다녀올 수 있어요.");
        }
        if (sentences.isEmpty()) sentences.add("지금 조건에 맞는 " + category.getLabel() + " 장소예요.");

        return String.join(" ", sentences.subList(0, Math.min(2, sentences.size())));
    }
}
