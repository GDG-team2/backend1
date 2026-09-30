package com.walkmission.domain.reward;

import com.walkmission.domain.reward.entity.Badge;
import com.walkmission.domain.reward.repository.BadgeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 기본 배지 도감. 서버를 켤 때마다 code 기준으로 동기화한다(없으면 추가, 있으면 이름·조건 갱신).
 * 배지를 추가하거나 문구를 바꾸려면 아래 목록만 고치면 된다.
 */
@Component
public class BadgeSeedInitializer implements ApplicationRunner {
    private final BadgeRepository badgeRepository;

    public BadgeSeedInitializer(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    private record Definition(String code, String name, String description, String condition) {}

    // TODO: 배지 아이콘 에셋이 준비되면 iconUrl 채우기
    private static final List<Definition> DEFINITIONS = List.of(
            new Definition("FIRST_MISSION", "첫 꼼지락", "첫 미션을 완료했어요.",
                    "{\"type\":\"MISSION_COUNT\",\"threshold\":1}"),
            new Definition("MISSION_10", "꼼지락 습관", "미션을 10번 완료했어요.",
                    "{\"type\":\"MISSION_COUNT\",\"threshold\":10}"),
            new Definition("NEW_PLACE_5", "새 길 발견", "서로 다른 장소 5곳을 방문했어요.",
                    "{\"type\":\"PLACE_COUNT\",\"threshold\":5}"),
            new Definition("RHYTHM_1", "첫 리듬", "처음으로 주간 목표를 채웠어요.",
                    "{\"type\":\"RHYTHM_WEEKS\",\"threshold\":1}"),
            new Definition("RHYTHM_4", "리듬 메이커", "주간 목표를 4주 연속 채웠어요.",
                    "{\"type\":\"RHYTHM_WEEKS\",\"threshold\":4}")
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Definition d : DEFINITIONS) {
            badgeRepository.findByCode(d.code()).ifPresentOrElse(
                    badge -> badge.updateDefinition(d.name(), d.description(), null, d.condition()),
                    () -> badgeRepository.save(new Badge(d.code(), d.name(), d.description(), null, d.condition())));
        }
    }
}
