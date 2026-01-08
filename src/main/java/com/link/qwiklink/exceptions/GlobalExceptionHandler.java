package com.link.qwiklink.exceptions;

import com.link.qwiklink.dtos.ResponseBody;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseBody<Void>> handleAppException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ResponseBody.error(ex.getStatus().value(), ex.getMessage()));
    }

    // validation errors from @Valid annotations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseBody<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseBody.error(HttpStatus.BAD_REQUEST.value(), msg));
    }

    // DB errors that weren't wrapped
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseBody<Void>> handleDb(DataAccessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseBody.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Database error"));
    }

    // final fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBody<Void>> handleAny(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseBody.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error"));
    }
}
