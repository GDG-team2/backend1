package com.walkmission.domain.reward.repository;

import com.walkmission.domain.reward.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    /** 현재 도감에 있는 배지 (code가 없는 옛 배지는 제외) */
    List<Badge> findByCodeIsNotNullOrderByIdAsc();
    Optional<Badge> findByCode(String code);
}
