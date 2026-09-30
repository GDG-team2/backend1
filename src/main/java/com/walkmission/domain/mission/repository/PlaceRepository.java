package com.walkmission.domain.mission.repository;

import com.walkmission.domain.mission.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByKakaoPlaceId(String kakaoPlaceId);
}
