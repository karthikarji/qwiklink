package com.link.qwiklink.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.link.qwiklink.exceptions.ErrorType;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorType type;

    public AppException(HttpStatus status, ErrorType type, String message) {
        super(message);
        this.status = status;
        this.type = type;
    }

    public AppException(HttpStatus status, ErrorType type, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.type = type;
    }
}
