package com.walkmission.domain.mission.repository;

/** 기간 내 완료 미션 집계 (JPQL 생성자 표현식용) */
public record HistoryStats(Long completedCount, Long totalDurationMinutes, Double averageSatisfaction,
                           Long totalStraightDistanceMeters, Long newPlaceCount) {}
