package com.walkmission.domain.reward.controller;

import com.walkmission.domain.reward.dto.BadgeListResponse;
import com.walkmission.domain.reward.dto.PointHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Reward Domain", description = "포인트 및 배지 보상 관련 API")
@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    @Operation(summary = "포인트 적립/사용 거래 내역 조회", description = "사용자의 포인트 적립 및 사용 내역을 페이징하여 조회합니다.")
    @GetMapping("/points/history")
    public ResponseEntity<PointHistoryResponse> getPointHistory(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        PointHistoryResponse response = new PointHistoryResponse(
                1500,
                List.of(
                        new PointHistoryResponse.PointHistoryEntry(1L, "EARN", 50, "미션 완주 보상", LocalDateTime.now().minusDays(1)),
                        new PointHistoryResponse.PointHistoryEntry(2L, "USE", -200, "기프티콘 교환", LocalDateTime.now().minusDays(2))
                ),
                new PointHistoryResponse.PaginationInfo(page, size, 5, 95L, true)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 배지 도감 및 보유 현황 조회", description = "시스템에 등록된 전체 배지 목록과 사용자의 획득 현황을 조회합니다.")
    @GetMapping("/badges")
    public ResponseEntity<BadgeListResponse> getBadges(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        BadgeListResponse response = new BadgeListResponse(
                new BadgeListResponse.BadgeSummary(20, 5),
                List.of(
                        new BadgeListResponse.BadgeDetail(
                                1L, "첫 산책 마스터", "첫 산책을 무사히 완료했습니다.", "https://example.com/badges/1.png",
                                true, true, LocalDateTime.now().minusDays(10)
                        ),
                        new BadgeListResponse.BadgeDetail(
                                2L, "연속 7일 달성", "7일 연속으로 산책 미션을 완료했습니다.", "https://example.com/badges/2.png",
                                false, false, null
                        )
                )
        );
        return ResponseEntity.ok(response);
    }
}
