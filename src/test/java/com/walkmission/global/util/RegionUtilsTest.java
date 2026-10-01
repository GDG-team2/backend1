package com.walkmission.global.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegionUtilsTest {

    @Test
    void 법정동_코드로_동_이름과_구_이름을_찾는다() {
        assertThat(RegionUtils.nameOf("1174010800")).isEqualTo("서울특별시 강동구 성내동");
        assertThat(RegionUtils.districtOf("1174010800")).isEqualTo("11740");
        assertThat(RegionUtils.districtNameOf("11740")).isEqualTo("서울특별시 강동구");
    }

    @Test
    void 구가_있는_시는_시와_구를_띄어_쓴다() {
        assertThat(RegionUtils.districtNameOf("41111")).isEqualTo("경기도 수원시 장안구");
    }

    @Test
    void 없는_코드는_기본_문구() {
        assertThat(RegionUtils.nameOf("9999910100")).isEqualTo(RegionUtils.UNKNOWN_NAME);
        assertThat(RegionUtils.isValid("11740")).isFalse();
        assertThat(RegionUtils.isValid("1174010800")).isTrue();
    }
}
