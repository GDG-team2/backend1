package com.walkmission.domain.mission.controller;

import com.walkmission.domain.mission.dto.*;
import com.walkmission.domain.mission.service.MissionService;
import com.walkmission.global.auth.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mission Domain", description = "산책 미션 관련 API")
@RestController
@RequestMapping("/api/v1/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @Operation(summary = "오늘의 미션 추천 생성", description = "현재 위치와 선호 설정(전체 외출 시간·이동수단·범주·예산)으로 카카오 장소 중 하나를 무작위로 추천합니다. 기분(mood)과 이번 추천에만 적용할 조건을 함께 보낼 수 있고, 다시 추천받으면 출발 전(READY) 미션은 대체되며 거절 이유(rejectReason)가 반영됩니다.")
    @PostMapping("/recommendation")
    public ResponseEntity<MissionRecommendResponse> recommendMission(
            @LoginUser Long userId,
            @Valid @RequestBody MissionRecommendRequest request) {
        return ResponseEntity.ok(missionService.recommend(userId, request));
    }

    @Operation(summary = "출발 약속 (예약) / 시간 변경", description = "추천받은 미션(READY)의 출발 시각을 약속합니다. 이미 약속한 미션에 다시 보내면 시간이 바뀝니다. 약속한 미션은 여러 개 둘 수 있고, 다시 추천을 받아도 대체되지 않습니다. 약속을 취소하려면 포기(abort)를 호출합니다.")
    @PutMapping("/{missionId}/schedule")
    public ResponseEntity<ScheduledMissionInfo> scheduleMission(
            @LoginUser Long userId,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionScheduleRequest request) {
        return ResponseEntity.ok(missionService.schedule(userId, missionId, request));
    }

    @Operation(summary = "예정 미션 목록", description = "출발을 약속한 미션을 가까운 시각 순으로 조회합니다. 시각이 지나도 패널티 없이 그대로 출발할 수 있습니다(isOverdue).")
    @GetMapping("/scheduled")
    public ResponseEntity<ScheduledMissionsResponse> getScheduledMissions(@LoginUser Long userId) {
        return ResponseEntity.ok(missionService.getScheduled(userId));
    }

    @Operation(summary = "산책 출발", description = "추천된 미션을 수락하고 산책을 시작합니다. 요청 본문은 없습니다. 이미 진행 중인 미션이 있으면 409를 반환합니다.")
    @PostMapping("/{missionId}/start")
    public ResponseEntity<MissionStartResponse> startMission(
            @LoginUser Long userId,
            @PathVariable Long missionId) {
        return ResponseEntity.ok(missionService.start(userId, missionId));
    }

    @Operation(summary = "GPS 목적지 도착 인증", description = "목적지 반경 80m 안에서 30초 머무르면 도착으로 인정합니다. 반경 안에서 처음 요청하면 status는 IN_PROGRESS이고, remainingDwellSeconds만큼 기다린 뒤 다시 요청하면 ARRIVED가 됩니다. 80m 밖이면 400 NOT_ENOUGH_DISTANCE와 남은 거리를 반환하고 체류 시간을 초기화합니다.")
    @PostMapping("/{missionId}/arrive")
    public ResponseEntity<MissionArriveResponse> arriveMission(
            @LoginUser Long userId,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionArriveRequest request) {
        return ResponseEntity.ok(missionService.arrive(userId, missionId, request));
    }

    @Operation(summary = "사후 설문 제출 및 최종 보상 정산", description = "도착 후 사후 기분 점수를 제출하고 미션을 완료합니다. 포인트, 주간 리듬, 랭킹 점수(주 3회까지, 새 범주 +20), 배지를 정산합니다.")
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<MissionCompleteResponse> completeMission(
            @LoginUser Long userId,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionCompleteRequest request) {
        return ResponseEntity.ok(missionService.complete(userId, missionId, request));
    }

    @Operation(summary = "산책 포기/중단", description = "진행 중인 미션을 멈춥니다. 이유와 이동 거리를 선택으로 보낼 수 있고, 이유가 \"멀었어요/피곤해요\"면 같은 날 다음 추천이 더 가까워집니다.")
    @PostMapping("/{missionId}/abort")
    public ResponseEntity<MissionAbortResponse> abortMission(
            @LoginUser Long userId,
            @PathVariable Long missionId,
            @Valid @RequestBody(required = false) MissionAbortRequest request) {
        return ResponseEntity.ok(missionService.abort(userId, missionId, request));
    }

    @Operation(summary = "진행 중인 미션 조회", description = "앱 재실행 시 복구를 위해 현재 진행 중(READY, IN_PROGRESS, ARRIVED)인 미션 상태를 확인합니다.")
    @GetMapping("/current")
    public ResponseEntity<CurrentMissionResponse> getCurrentMission(@LoginUser Long userId) {
        return ResponseEntity.ok(missionService.getCurrent(userId));
    }
}
