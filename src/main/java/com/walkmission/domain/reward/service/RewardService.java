package com.walkmission.domain.reward.service;

import com.walkmission.domain.reward.entity.PointHistory;
import com.walkmission.domain.reward.entity.PointType;
import com.walkmission.domain.reward.repository.PointHistoryRepository;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.entity.UserProfile;
import com.walkmission.domain.user.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RewardService {
    private final PointHistoryRepository pointHistoryRepository;
    private final UserProfileRepository userProfileRepository;

    public RewardService(PointHistoryRepository pointHistoryRepository, UserProfileRepository userProfileRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /** 포인트를 적립하고 적립 후 보유 포인트를 반환한다. */
    @Transactional
    public int earnPoint(User user, int amount, String description) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        profile.addPoint(amount);
        pointHistoryRepository.save(new PointHistory(user, amount, PointType.EARN, description));
        return profile.getCurrentPoint();
    }
}
