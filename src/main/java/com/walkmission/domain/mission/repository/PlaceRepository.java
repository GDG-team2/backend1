package com.walkmission.domain.mission.repository;

import com.walkmission.domain.mission.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByIsClosedFalseAndLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLatitude, BigDecimal maxLatitude, BigDecimal minLongitude, BigDecimal maxLongitude);
}
