package com.walkmission.domain.ranking.controller;

import com.walkmission.domain.ranking.dto.RegionRankingResponse;
import com.walkmission.domain.ranking.service.RankingService;
import com.walkmission.global.auth.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ranking Domain", description = "랭킹 리더보드 관련 API")
@RestController
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Operation(summary = "내 동네 주간 랭킹 리더보드 조회", description = "현재 사용자가 속한 동네의 이번 주(월~일) 랭킹을 조회합니다. 랭킹 공개 설정을 끈 사용자는 리더보드에서 제외됩니다. limit 최대 100.")
    @GetMapping("/my-region")
    public ResponseEntity<RegionRankingResponse> getMyRegionRanking(
            @LoginUser Long userId,
            @RequestParam(defaultValue = "50") Integer limit) {
        return ResponseEntity.ok(rankingService.getMyRegionRanking(userId, limit));
    }
}
