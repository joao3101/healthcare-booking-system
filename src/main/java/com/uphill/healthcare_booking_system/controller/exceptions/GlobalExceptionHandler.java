package com.uphill.healthcare_booking_system.controller.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.uphill.healthcare_booking_system.domain.exceptions.InvalidAppointmentWindowException;
import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableDoctorException;
import com.uphill.healthcare_booking_system.domain.exceptions.NoAvailableRoomException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Object> buildError(HttpStatus status, String message, Exception ex) {
        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message,
                        "exception", ex.getClass().getSimpleName()
                ));
    }

    @ExceptionHandler(InvalidAppointmentWindowException.class)
    public ResponseEntity<Object> handleInvalidWindow(InvalidAppointmentWindowException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid appointment time window", ex);
    }

    @ExceptionHandler(NoAvailableDoctorException.class)
    public ResponseEntity<Object> handleNoDoctor(NoAvailableDoctorException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), ex);
    }

    @ExceptionHandler(NoAvailableRoomException.class)
    public ResponseEntity<Object> handleNoRoom(NoAvailableRoomException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrityViolation(DataIntegrityViolationException ex) {
        return buildError(HttpStatus.CONFLICT, "Data integrity violation", ex);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Object> handlePessimisticLock(PessimisticLockingFailureException ex) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, "Resource is locked, try again later", ex);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return buildError(HttpStatus.CONFLICT, "Concurrent modification detected", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex);
    }
}