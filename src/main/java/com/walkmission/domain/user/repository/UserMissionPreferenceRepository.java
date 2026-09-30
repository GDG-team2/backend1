package com.walkmission.domain.user.repository;
import com.walkmission.domain.user.entity.UserMissionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserMissionPreferenceRepository extends JpaRepository<UserMissionPreference, Long> {
    Optional<UserMissionPreference> findByUserId(Long userId);
}
