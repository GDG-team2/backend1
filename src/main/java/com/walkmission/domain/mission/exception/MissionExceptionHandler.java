package com.walkmission.domain.mission.exception;

import com.walkmission.domain.mission.controller.MissionController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// TODO(Phase 4): GlobalExceptionHandler 도입 시 공통 에러 포맷으로 통합
@RestControllerAdvice(assignableTypes = MissionController.class)
public class MissionExceptionHandler {

    @ExceptionHandler(NotEnoughDistanceException.class)
    public ResponseEntity<NotEnoughDistanceResponse> handleNotEnoughDistance(NotEnoughDistanceException e) {
        return ResponseEntity.badRequest().body(
                new NotEnoughDistanceResponse("NOT_ENOUGH_DISTANCE", e.getMessage(), e.getCurrentDistanceMeters()));
    }
}
