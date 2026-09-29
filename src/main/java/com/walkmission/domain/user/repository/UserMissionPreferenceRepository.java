package com.walkmission.domain.user.repository;
import com.walkmission.domain.user.entity.UserMissionPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMissionPreferenceRepository extends JpaRepository<UserMissionPreference, Long> {
}
