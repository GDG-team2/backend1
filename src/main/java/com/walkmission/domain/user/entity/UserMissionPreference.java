package com.walkmission.domain.user.entity;

import com.walkmission.domain.mission.entity.Budget;
import com.walkmission.domain.mission.entity.MoveType;
import com.walkmission.domain.mission.entity.PlaceCategory;
import com.walkmission.domain.mission.entity.PlaceCategorySetConverter;
import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.EnumSet;
import java.util.Set;

@Entity
public class UserMissionPreference extends BaseTimeEntity {
    public static final int DEFAULT_WALK_TIME = 60;
    public static final int DEFAULT_WEEKLY_GOAL = 3;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Integer walkTime;
    private Integer spendLimit;
    /** 주간 리듬 목표(주 N회) */
    private Integer weeklyGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "how_move")
    private MoveType moveType;

    /** 선호 범주. 비어 있으면 전체 범주 */
    @Convert(converter = PlaceCategorySetConverter.class)
    @Column(name = "preference")
    private Set<PlaceCategory> categories = EnumSet.noneOf(PlaceCategory.class);

    protected UserMissionPreference() {}

    public UserMissionPreference(User user) {
        this.user = user;
        this.walkTime = DEFAULT_WALK_TIME;
        this.moveType = MoveType.WALK;
        this.weeklyGoal = DEFAULT_WEEKLY_GOAL;
    }

    public void update(Integer walkTime, MoveType moveType, Set<PlaceCategory> categories, Integer weeklyGoal,
                       Budget budget) {
        if (budget != null) this.spendLimit = budget.getLimit();
        if (walkTime != null) this.walkTime = walkTime;
        if (weeklyGoal != null) this.weeklyGoal = weeklyGoal;
        if (moveType != null) this.moveType = moveType;
        if (categories != null) {
            this.categories = categories.isEmpty() ? EnumSet.noneOf(PlaceCategory.class) : EnumSet.copyOf(categories);
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public int getWalkTime() { return walkTime != null ? walkTime : DEFAULT_WALK_TIME; }
    public Integer getSpendLimit() { return spendLimit; }
    /** spend_limit(원)을 예산 선택지로. 설정하지 않았으면 상관없음 */
    public Budget getBudget() { return Budget.fromLimit(spendLimit); }
    public MoveType getMoveType() { return moveType != null ? moveType : MoveType.WALK; }
    public int getWeeklyGoal() { return weeklyGoal != null ? weeklyGoal : DEFAULT_WEEKLY_GOAL; }

    /** 선택한 범주가 없으면 전체 범주를 반환한다. */
    public Set<PlaceCategory> getEffectiveCategories() {
        return categories == null || categories.isEmpty()
                ? EnumSet.allOf(PlaceCategory.class)
                : EnumSet.copyOf(categories);
    }
}
