package com.walkmission.domain.mission;

import com.walkmission.domain.mission.entity.Place;
import com.walkmission.domain.mission.repository.PlaceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 로컬 개발용 테스트 장소 데이터. Place 테이블이 비어 있을 때만 넣는다.
 * TODO: Kakao Local API 연동 후 제거
 */
@Component
@Profile("local")
public class PlaceSeedInitializer implements ApplicationRunner {
    private final PlaceRepository placeRepository;

    public PlaceSeedInitializer(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (placeRepository.count() > 0) return;

        placeRepository.saveAll(List.of(
                place("seed-001", "[테스트] 역삼 근린공원", "공원", "37.4990000", "127.0280000"),
                place("seed-002", "[테스트] 강남 북카페", "카페", "37.5012000", "127.0265000"),
                place("seed-003", "[테스트] 서초 산책공원", "공원", "37.4945000", "127.0225000"),
                place("seed-004", "[테스트] 테헤란 작은도서관", "도서관", "37.5030000", "127.0340000"),
                place("seed-005", "[테스트] 논현 갤러리", "전시관", "37.5080000", "127.0220000")
        ));
    }

    private Place place(String id, String name, String category, String lat, String lon) {
        return new Place(id, name, "서울특별시 강남구 (테스트 주소)", category, new BigDecimal(lat), new BigDecimal(lon));
    }
}
