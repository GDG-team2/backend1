package com.walkmission.domain.mission.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 기간 내 방문한 장소별 집계 (JPQL 생성자 표현식용) */
public record VisitedPlaceRow(Long placeId, String name, String category, BigDecimal latitude, BigDecimal longitude,
                              Long visitCount, LocalDateTime lastVisitedAt) {}
