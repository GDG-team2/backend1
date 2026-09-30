package com.walkmission.domain.reward.controller;

import com.walkmission.domain.reward.dto.BadgeListResponse;
import com.walkmission.domain.reward.dto.PointHistoryResponse;
import com.walkmission.domain.reward.service.RewardService;
import com.walkmission.global.auth.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reward Domain", description = "포인트 및 배지 보상 관련 API")
@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @Operation(summary = "포인트 적립/사용 거래 내역 조회", description = "사용자의 포인트 적립 및 사용 내역을 최신순으로 페이징하여 조회합니다. size 최대 100.")
    @GetMapping("/points/history")
    public ResponseEntity<PointHistoryResponse> getPointHistory(
            @LoginUser Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(rewardService.getPointHistory(userId, page, size));
    }

    @Operation(summary = "전체 배지 도감 및 보유 현황 조회", description = "시스템에 등록된 전체 배지 목록과 사용자의 획득 현황을 조회합니다.")
    @GetMapping("/badges")
    public ResponseEntity<BadgeListResponse> getBadges(@LoginUser Long userId) {
        return ResponseEntity.ok(rewardService.getBadges(userId));
    }
}
