package com.walkmission.domain.mission.entity;

import com.walkmission.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 미션 상태 순서: READY → IN_PROGRESS → ARRIVED → COMPLETED, 진행 중 어디서든 ABORTED */
class MissionRecordTest {
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 9, 30, 17, 0);

    private MissionRecord newMission() {
        User user = new User("a@test.local", "pw", "a", null, "1");
        Place place = new Place("k1", "화랑유원지", "주소", "도시근린공원",
                new BigDecimal("37.3"), new BigDecimal("126.8"), null);
        return new MissionRecord(user, place, PlaceCategory.WALK, true, "화랑유원지 한 바퀴", "이유", Mood.GLOOMY,
                MoveType.WALK, 800);
    }

    @Test
    void 정상_순서로_진행하면_완료되고_소요_시간이_저장된다() {
        MissionRecord m = newMission();
        m.start(T0);
        m.arrive(T0.plusMinutes(20));
        m.complete(5, 3000, T0.plusMinutes(54));

        assertThat(m.getStatus()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(m.getDurationMinutes()).isEqualTo(54);
        assertThat(m.getAfterSurvey()).isEqualTo(5);
    }

    @Test
    void 추천받을_때_고른_기분이_산책_전_점수로_저장된다() {
        MissionRecord m = newMission();
        assertThat(m.getBeforeMood()).isEqualTo(Mood.GLOOMY);
        assertThat(m.getBeforeSurvey()).isEqualTo(Mood.GLOOMY.getScore());
    }

    @Test
    void 출발하지_않고_도착할_수_없다() {
        MissionRecord m = newMission();
        assertThatThrownBy(() -> m.arrive(T0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 도착하지_않고_완료할_수_없다() {
        MissionRecord m = newMission();
        m.start(T0);
        assertThatThrownBy(() -> m.complete(5, null, T0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 도착_체류는_처음_들어온_시각을_유지하고_반경을_벗어나면_초기화된다() {
        MissionRecord m = newMission();
        m.start(T0);
        assertThat(m.markInsideArrivalZone(T0.plusMinutes(10))).isEqualTo(T0.plusMinutes(10));
        assertThat(m.markInsideArrivalZone(T0.plusMinutes(11))).isEqualTo(T0.plusMinutes(10));

        m.resetArrivalCheck();
        assertThat(m.markInsideArrivalZone(T0.plusMinutes(12))).isEqualTo(T0.plusMinutes(12));
    }

    @Test
    void 완료한_미션은_포기할_수_없다() {
        MissionRecord m = newMission();
        m.start(T0);
        m.arrive(T0);
        m.complete(4, null, T0);
        assertThatThrownBy(() -> m.abort(null, null, T0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 포기하면_이유와_이동_거리가_남는다() {
        MissionRecord m = newMission();
        m.start(T0);
        m.abort(AbortReason.TIRED, 700, T0.plusMinutes(10));
        assertThat(m.getStatus()).isEqualTo(MissionStatus.ABORTED);
        assertThat(m.getAbortReason()).isEqualTo(AbortReason.TIRED);
        assertThat(m.getMovedDistanceMeters()).isEqualTo(700);
    }

    @Test
    void 다시_추천으로_대체되면_거절_이유가_남는다() {
        MissionRecord m = newMission();
        m.replaceByNewRecommendation(RejectReason.TOO_FAR, T0);
        assertThat(m.getStatus()).isEqualTo(MissionStatus.ABORTED);
        assertThat(m.getRejectReason()).isEqualTo(RejectReason.TOO_FAR);
    }

    @Test
    void 약속은_출발_전에만_할_수_있다() {
        MissionRecord m = newMission();
        m.schedule(T0.plusHours(1));
        assertThat(m.getScheduledAt()).isEqualTo(T0.plusHours(1));
        m.start(T0);
        assertThatThrownBy(() -> m.schedule(T0.plusHours(2))).isInstanceOf(IllegalStateException.class);
    }
}
