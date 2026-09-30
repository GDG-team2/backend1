package com.walkmission.domain.mission.exception;

public class NotEnoughDistanceException extends RuntimeException {
    private final int currentDistanceMeters;

    public NotEnoughDistanceException(int currentDistanceMeters) {
        super("목적지 반경 50m 이내에 도착하지 않았습니다.");
        this.currentDistanceMeters = currentDistanceMeters;
    }

    public int getCurrentDistanceMeters() { return currentDistanceMeters; }
}
