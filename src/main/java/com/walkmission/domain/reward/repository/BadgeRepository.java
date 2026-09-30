package com.walkmission.domain.reward.repository;

import com.walkmission.domain.reward.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findAllByOrderByIdAsc();
}
