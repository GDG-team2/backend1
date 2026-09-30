package com.walkmission.domain.ranking.entity;

import com.walkmission.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RankingTest {

    @Test
    void 점수와_반영_횟수와_보너스가_쌓이고_새_주에_초기화된다() {
        LocalDate week = LocalDate.of(2026, 9, 28);
        Ranking r = new Ranking(new User("a@test.local", "pw", "a", null, "1"), week);
        r.addMissionScore(100, 20);
        r.addMissionScore(100, 0);

        assertThat(r.getUserScore()).isEqualTo(220);
        assertThat(r.getScoredMissionCount()).isEqualTo(2);
        assertThat(r.getBonusScore()).isEqualTo(20);

        r.resetForWeek(week.plusWeeks(1));
        assertThat(r.getUserScore()).isZero();
        assertThat(r.getScoredMissionCount()).isZero();
        assertThat(r.getWeekStartDate()).isEqualTo(week.plusWeeks(1));
    }
}
