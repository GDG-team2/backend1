package com.walkmission.global.util;

import java.math.BigDecimal;

public final class GeoUtils {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private GeoUtils() {}

    /** 하버사인 공식으로 두 좌표 사이의 거리(미터)를 계산한다. */
    public static double distanceMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double phi1 = Math.toRadians(lat1.doubleValue());
        double phi2 = Math.toRadians(lat2.doubleValue());
        double dPhi = phi2 - phi1;
        double dLambda = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }
}
