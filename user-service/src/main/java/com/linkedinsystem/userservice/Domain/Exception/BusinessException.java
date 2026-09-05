package com.linkedinsystem.userservice.Domain.Exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private String issuer;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.issuer = issuer;
    }

    public HttpStatus getStatus() { return status; }

    public String getIssuer() { return issuer; }
}
