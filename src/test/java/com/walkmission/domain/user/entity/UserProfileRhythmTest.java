package com.walkmission.domain.user.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** 주간 리듬: 주간 목표를 연속으로 채운 주 수 */
class UserProfileRhythmTest {
    private static final LocalDate W1 = LocalDate.of(2026, 9, 7);   // 월요일

    private UserProfile newProfile() {
        return new UserProfile(new User("a@test.local", "pw", "a", null, "1"));
    }

    @Test
    void 처음_달성하면_1주() {
        UserProfile p = newProfile();
        assertThat(p.achieveWeeklyGoal(W1)).isTrue();
        assertThat(p.getRhythmWeeksAsOf(W1)).isEqualTo(1);
        assertThat(p.getBestRhythmWeeks()).isEqualTo(1);
    }

    @Test
    void 같은_주에_다시_달성해도_늘지_않는다() {
        UserProfile p = newProfile();
        p.achieveWeeklyGoal(W1);
        assertThat(p.achieveWeeklyGoal(W1)).isFalse();
        assertThat(p.getRhythmWeeksAsOf(W1)).isEqualTo(1);
    }

    @Test
    void 연속된_주에_달성하면_이어진다() {
        UserProfile p = newProfile();
        p.achieveWeeklyGoal(W1);
        p.achieveWeeklyGoal(W1.plusWeeks(1));
        p.achieveWeeklyGoal(W1.plusWeeks(2));
        assertThat(p.getRhythmWeeksAsOf(W1.plusWeeks(2))).isEqualTo(3);
        assertThat(p.getBestRhythmWeeks()).isEqualTo(3);
    }

    @Test
    void 한_주를_건너뛰면_1주부터_다시_시작하고_최장_기록은_남는다() {
        UserProfile p = newProfile();
        p.achieveWeeklyGoal(W1);
        p.achieveWeeklyGoal(W1.plusWeeks(1));
        p.achieveWeeklyGoal(W1.plusWeeks(3));
        assertThat(p.getRhythmWeeksAsOf(W1.plusWeeks(3))).isEqualTo(1);
        assertThat(p.getBestRhythmWeeks()).isEqualTo(2);
    }

    @Test
    void 이번_주는_아직_진행_중이라_지난주에_달성했으면_유지로_본다() {
        UserProfile p = newProfile();
        p.achieveWeeklyGoal(W1);
        assertThat(p.getRhythmWeeksAsOf(W1.plusWeeks(1))).isEqualTo(1);
    }

    @Test
    void 지난주를_놓쳤으면_끊긴_것으로_본다() {
        UserProfile p = newProfile();
        p.achieveWeeklyGoal(W1);
        assertThat(p.getRhythmWeeksAsOf(W1.plusWeeks(2))).isZero();
    }
}
