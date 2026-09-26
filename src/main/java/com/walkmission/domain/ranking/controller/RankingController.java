package com.walkmission.domain.ranking.controller;

import com.walkmission.domain.ranking.dto.RegionRankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Ranking Domain", description = "랭킹 리더보드 관련 API")
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    @Operation(summary = "내 동네 주간 랭킹 리더보드 조회", description = "현재 사용자가 속한 동네의 주간 랭킹 리더보드를 조회합니다.")
    @GetMapping("/my-region")
    public ResponseEntity<RegionRankingResponse> getMyRegionRanking(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "50") Integer limit) {
        
        RegionRankingResponse response = new RegionRankingResponse(
                new RegionRankingResponse.RegionInfo("1168010100", "서울특별시 강남구 역삼동"),
                new RegionRankingResponse.WeekPeriodInfo(LocalDate.now().minusDays(3), LocalDate.now().plusDays(3)),
                new RegionRankingResponse.RankingEntry(
                        true, UUID.randomUUID(), "walking_master", "https://example.com/profile.png", 5, 800
                ),
                List.of(
                        new RegionRankingResponse.RankingEntry(
                                null, UUID.randomUUID(), "1등_산책왕", null, 1, 1500
                        ),
                        new RegionRankingResponse.RankingEntry(
                                null, UUID.randomUUID(), "2등_뚜벅이", "https://example.com/p2.png", 2, 1200
                        )
                )
        );
        return ResponseEntity.ok(response);
    }
}
