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

    @Operation(summary = "오늘의 미션 추천 생성", description = "사용자의 현재 위치를 기반으로 반경 3km 이내에서 가장 가까운 장소를 미션 목적지로 추천합니다. 출발 전(READY) 미션이 있으면 새 추천으로 대체됩니다.")
    @PostMapping("/recommendation")
    public ResponseEntity<MissionRecommendResponse> recommendMission(
            @LoginUser Long userId,
            @Valid @RequestBody MissionRecommendRequest request) {
        return ResponseEntity.ok(missionService.recommend(userId, request));
    }

    @Operation(summary = "산책 출발", description = "추천된 미션을 수락하고 산책을 시작합니다. 사전 설문 점수는 선택이며, 이미 진행 중인 미션이 있으면 409를 반환합니다.")
    @PostMapping("/{missionId}/start")
    public ResponseEntity<MissionStartResponse> startMission(
            @LoginUser Long userId,
            @PathVariable Long missionId,
            @Valid @RequestBody(required = false) MissionStartRequest request) {
        return ResponseEntity.ok(missionService.start(userId, missionId, request));
    }

    @Operation(summary = "GPS 목적지 도착 인증", description = "목적지 반경 50m 이내에 도달했는지 확인하고 도착 인증을 처리합니다. 50m를 초과하면 400 NOT_ENOUGH_DISTANCE와 남은 거리를 반환합니다.")
    @PostMapping("/{missionId}/arrive")
    public ResponseEntity<MissionArriveResponse> arriveMission(
            @LoginUser Long userId,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionArriveRequest request) {
        return ResponseEntity.ok(missionService.arrive(userId, missionId, request));
    }

    @Operation(summary = "사후 설문 제출 및 최종 보상 정산", description = "도착 후 설문을 제출하고 미션을 최종 완료하여 포인트를 적립합니다. (랭킹/스트릭/배지 정산은 추후 반영)")
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<MissionCompleteResponse> completeMission(
            @LoginUser Long userId,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionCompleteRequest request) {
        return ResponseEntity.ok(missionService.complete(userId, missionId, request));
    }

    @Operation(summary = "산책 포기/중단", description = "진행 중인 미션을 포기합니다.")
    @PostMapping("/{missionId}/abort")
    public ResponseEntity<MissionAbortResponse> abortMission(
            @LoginUser Long userId,
            @PathVariable Long missionId) {
        return ResponseEntity.ok(missionService.abort(userId, missionId));
    }

    @Operation(summary = "진행 중인 미션 조회", description = "앱 재실행 시 복구를 위해 현재 진행 중(READY, IN_PROGRESS, ARRIVED)인 미션 상태를 확인합니다.")
    @GetMapping("/current")
    public ResponseEntity<CurrentMissionResponse> getCurrentMission(@LoginUser Long userId) {
        return ResponseEntity.ok(missionService.getCurrent(userId));
    }
}
