package com.realworld.webfluxfn.exception;

import lombok.Getter;

public class InvalidRequestException extends RuntimeException {
    private static final long serialVersionUID = -1;

    @Getter
    private final String subject;
    @Getter
    private final String violation;

    public InvalidRequestException(final String subject, final String violation) {
        super(subject + ": " + violation);
        this.subject = subject;
        this.violation = violation;
    }

    public InvalidRequestException(final String subject, final String violation, final Throwable cause) {
        super(subject + ": " + violation, cause);
        this.subject = subject;
        this.violation = violation;
    }
}
