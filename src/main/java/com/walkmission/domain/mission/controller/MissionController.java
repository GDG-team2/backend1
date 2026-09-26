package com.walkmission.domain.mission.controller;

import com.walkmission.domain.mission.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Mission Domain", description = "산책 미션 관련 API")
@RestController
@RequestMapping("/api/v1/missions")
public class MissionController {

    @Operation(summary = "오늘의 미션 추천 생성", description = "사용자의 현재 위치를 기반으로 적합한 미션 목적지를 추천합니다.")
    @PostMapping("/recommendation")
    public ResponseEntity<MissionRecommendResponse> recommendMission(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody MissionRecommendRequest request) {
        
        MissionRecommendResponse response = new MissionRecommendResponse(
                1001L,
                "READY",
                new MissionRecommendResponse.PlaceInfo(
                        2001L, "18577297", "역삼동 근린공원", "공원", "서울특별시 강남구 역삼로 123",
                        new BigDecimal("37.499000"), new BigDecimal("127.028000")
                ),
                450,
                10,
                50
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "산책 출발", description = "추천된 미션을 수락하고 산책을 시작합니다.")
    @PostMapping("/{missionId}/start")
    public ResponseEntity<MissionStartResponse> startMission(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long missionId) {
        
        MissionStartResponse response = new MissionStartResponse(
                missionId,
                "IN_PROGRESS",
                LocalDateTime.now(),
                "산책 미션을 시작했습니다. 안전하게 이동하세요!"
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "GPS 목적지 도착 인증", description = "목적지 반경 50m 이내에 도달했는지 확인하고 도착 인증을 처리합니다.")
    @PostMapping("/{missionId}/arrive")
    public ResponseEntity<MissionArriveResponse> arriveMission(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionArriveRequest request) {
        
        MissionArriveResponse response = new MissionArriveResponse(
                missionId,
                "ARRIVED",
                LocalDateTime.now(),
                "목적지에 도착했습니다! 주변을 둘러보세요."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사후 설문 제출 및 최종 보상 정산", description = "도착 후 설문을 제출하고 미션을 최종 완료하여 포인트/배지/랭킹 보상을 정산합니다.")
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<MissionCompleteResponse> completeMission(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long missionId,
            @Valid @RequestBody MissionCompleteRequest request) {
        
        MissionCompleteResponse response = new MissionCompleteResponse(
                missionId,
                "COMPLETED",
                LocalDateTime.now(),
                request.stepCount() != null ? request.stepCount() : 1850,
                new MissionCompleteResponse.RewardInfo(50, 1550),
                new MissionCompleteResponse.RankingInfo(true, 100, 500),
                new MissionCompleteResponse.StreakInfo(6, true),
                List.of(
                        new MissionCompleteResponse.BadgeInfo(2L, "공원 탐험가", "공원 카테고리의 장소를 3번 방문했습니다.", "https://example.com/badges/2.png")
                )
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "산책 포기/중단", description = "진행 중인 미션을 포기하고 상태를 초기화합니다.")
    @PostMapping("/{missionId}/abort")
    public ResponseEntity<MissionAbortResponse> abortMission(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long missionId) {
        
        MissionAbortResponse response = new MissionAbortResponse(
                missionId,
                "ABORTED",
                LocalDateTime.now(),
                "산책 미션을 포기했습니다."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "진행 중인 미션 조회", description = "앱 재실행 시 복구를 위해 현재 진행 중(READY, IN_PROGRESS, ARRIVED)인 미션 상태를 확인합니다.")
    @GetMapping("/current")
    public ResponseEntity<CurrentMissionResponse> getCurrentMission(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        CurrentMissionResponse response = new CurrentMissionResponse(
                true,
                new CurrentMissionResponse.ActiveMissionInfo(
                        1001L,
                        "IN_PROGRESS",
                        LocalDateTime.now().minusMinutes(5),
                        new CurrentMissionResponse.PlaceInfo(
                                2001L, "18577297", "역삼동 근린공원", "공원", "서울특별시 강남구 역삼로 123",
                                new BigDecimal("37.499000"), new BigDecimal("127.028000")
                        ),
                        50
                )
        );
        return ResponseEntity.ok(response);
    }
}
