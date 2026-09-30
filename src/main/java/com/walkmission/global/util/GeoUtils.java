package com.walkmission.global.util;

import java.math.BigDecimal;

public final class GeoUtils {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private GeoUtils() {}

    /** 하버사인 공식으로 두 좌표 사이의 거리(미터)를 계산한다. */
    public static double distanceMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        return distanceMeters(lat1.doubleValue(), lon1.doubleValue(), lat2.doubleValue(), lon2.doubleValue());
    }

    public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = phi2 - phi1;
        double dLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }

    /** 시작점에서 방위각(도, 북쪽 0 · 시계방향)으로 distanceMeters만큼 이동한 지점의 {위도, 경도} */
    public static double[] destination(double latitude, double longitude, double bearingDegrees, double distanceMeters) {
        double delta = distanceMeters / EARTH_RADIUS_METERS;
        double theta = Math.toRadians(bearingDegrees);
        double phi1 = Math.toRadians(latitude);
        double lambda1 = Math.toRadians(longitude);

        double phi2 = Math.asin(Math.sin(phi1) * Math.cos(delta) + Math.cos(phi1) * Math.sin(delta) * Math.cos(theta));
        double lambda2 = lambda1 + Math.atan2(Math.sin(theta) * Math.sin(delta) * Math.cos(phi1),
                Math.cos(delta) - Math.sin(phi1) * Math.sin(phi2));
        return new double[]{Math.toDegrees(phi2), Math.toDegrees(lambda2)};
    }
}
