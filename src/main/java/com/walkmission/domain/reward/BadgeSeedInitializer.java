package com.walkmission.domain.reward;

import com.walkmission.domain.reward.entity.Badge;
import com.walkmission.domain.reward.repository.BadgeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 기본 배지 도감. Badge 테이블이 비어 있을 때만 넣는다. */
@Component
public class BadgeSeedInitializer implements ApplicationRunner {
    private final BadgeRepository badgeRepository;

    public BadgeSeedInitializer(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (badgeRepository.count() > 0) return;

        // TODO: 배지 아이콘 에셋이 준비되면 iconUrl 채우기
        badgeRepository.saveAll(List.of(
                new Badge("첫 산책", "첫 미션을 완료했어요.", null, "{\"type\":\"MISSION_COUNT\",\"threshold\":1}"),
                new Badge("산책 습관", "미션을 10번 완료했어요.", null, "{\"type\":\"MISSION_COUNT\",\"threshold\":10}"),
                new Badge("작심삼일 돌파", "3일 연속으로 산책했어요.", null, "{\"type\":\"STREAK\",\"threshold\":3}"),
                new Badge("일주일 산책러", "7일 연속으로 산책했어요.", null, "{\"type\":\"STREAK\",\"threshold\":7}"),
                new Badge("동네 탐험가", "서로 다른 장소 5곳을 방문했어요.", null, "{\"type\":\"PLACE_COUNT\",\"threshold\":5}")
        ));
    }
}
