package com.walkmission.domain.mission.entity;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetAndCategoryTest {

    @Test
    void 예산을_넘는_범주는_허용하지_않는다() {
        assertThat(Budget.FREE.allows(PlaceCategory.WALK.getEstimatedCost())).isTrue();
        assertThat(Budget.FREE.allows(PlaceCategory.CAFE.getEstimatedCost())).isFalse();
        assertThat(Budget.UNDER_10K.allows(PlaceCategory.EXHIBITION.getEstimatedCost())).isTrue();   // 10,000원 이하
        assertThat(Budget.UNDER_10K.allows(PlaceCategory.FOOD.getEstimatedCost())).isFalse();        // 12,000원
        assertThat(Budget.ANY.allows(1_000_000)).isTrue();
    }

    @Test
    void 저장된_예산_금액을_선택지로_되돌린다() {
        assertThat(Budget.fromLimit(null)).isEqualTo(Budget.ANY);
        assertThat(Budget.fromLimit(0)).isEqualTo(Budget.FREE);
        assertThat(Budget.fromLimit(10_000)).isEqualTo(Budget.UNDER_10K);
        assertThat(Budget.fromLimit(12_345)).isEqualTo(Budget.ANY);
    }

    @Test
    void 선호_범주는_문자열로_저장했다가_그대로_복원된다() {
        PlaceCategorySetConverter converter = new PlaceCategorySetConverter();
        Set<PlaceCategory> categories = EnumSet.of(PlaceCategory.WALK, PlaceCategory.CAFE);

        String column = converter.convertToDatabaseColumn(categories);
        assertThat(column).isEqualTo("CAFE,WALK");
        assertThat(converter.convertToEntityAttribute(column))
                .containsExactlyInAnyOrder(PlaceCategory.WALK, PlaceCategory.CAFE);
    }

    @Test
    void 빈_값이나_모르는_범주는_무시한다() {
        PlaceCategorySetConverter converter = new PlaceCategorySetConverter();
        assertThat(converter.convertToDatabaseColumn(EnumSet.noneOf(PlaceCategory.class))).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("WALK,UNKNOWN")).containsExactly(PlaceCategory.WALK);
    }
}
