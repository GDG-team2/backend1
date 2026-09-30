package com.walkmission.global.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoUtilsTest {

    @Test
    void 적도에서_경도_1도는_약_111킬로미터() {
        double meters = GeoUtils.distanceMeters(0, 0, 0, 1);
        assertThat(meters).isCloseTo(111_195, within(1.0));
    }

    @Test
    void 같은_지점은_거리_0() {
        BigDecimal lat = new BigDecimal("37.498095");
        BigDecimal lon = new BigDecimal("127.027610");
        assertThat(GeoUtils.distanceMeters(lat, lon, lat, lon)).isZero();
    }

    @Test
    void 방위각으로_이동한_지점까지의_거리는_이동한_거리와_같다() {
        for (double bearing : new double[]{0, 45, 90, 180, 270}) {
            double[] p = GeoUtils.destination(37.498095, 127.027610, bearing, 500);
            assertThat(GeoUtils.distanceMeters(37.498095, 127.027610, p[0], p[1])).isCloseTo(500, within(0.5));
        }
    }

    @Test
    void 북쪽으로_이동하면_위도만_커진다() {
        double[] p = GeoUtils.destination(37.5, 127.0, 0, 1000);
        assertThat(p[0]).isGreaterThan(37.5);
        assertThat(p[1]).isCloseTo(127.0, within(1e-9));
    }
}
