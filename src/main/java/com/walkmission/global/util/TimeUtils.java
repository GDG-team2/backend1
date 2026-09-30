package com.walkmission.global.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public final class TimeUtils {
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private TimeUtils() {}

    /** 스트릭/주간 랭킹의 날짜 기준(한국 시간) */
    public static LocalDate today() {
        return LocalDate.now(KST);
    }

    /** 해당 날짜가 속한 주의 월요일 */
    public static LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
