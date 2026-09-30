package com.walkmission.domain.user.repository;
import com.walkmission.domain.user.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    Optional<UserSetting> findByUserId(Long userId);

    @Query("select s.user.id from UserSetting s where s.rankingSetting = true and s.user.id in :userIds")
    List<Long> findRankingPublicUserIds(@Param("userIds") Collection<Long> userIds);
}
