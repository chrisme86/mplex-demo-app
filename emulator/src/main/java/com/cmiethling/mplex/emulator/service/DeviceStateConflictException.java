package com.cmiethling.mplex.emulator.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * The command is not allowed in the current device state. Spring sends it as 409 ProblemDetail
 * (application/problem+json).
 */
public class DeviceStateConflictException extends ErrorResponseException {

    public DeviceStateConflictException(final String detail) {
        super(HttpStatus.CONFLICT, ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail), null);
    }
}
