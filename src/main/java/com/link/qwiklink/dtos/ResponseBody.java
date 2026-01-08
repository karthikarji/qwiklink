package com.link.qwiklink.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Standard API response wrapper for all REST endpoints.
 *
 * @param <T> type of response payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseBody<T> {

    /**
     * Indicates whether the request was processed successfully.
     */
    private boolean success;

    /**
     * HTTP status code returned to the client.
     */
    private int statusCode;

    /**
     * Human-readable message describing the outcome.
     */
    private String message;

    /**
     * Actual response payload (can be null).
     */
    private T data;

    /* -----------------------
       Factory helper methods
       ----------------------- */

    public static <T> ResponseBody<T> success(HttpStatus status, String message, T data) {
        return ResponseBody.<T>builder()
                .success(true)
                .statusCode(status.value())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseBody<T> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> ResponseBody<T> created(String message, T data) {
        return success(HttpStatus.CREATED, message, data);
    }

    public static <T> ResponseBody<T> error(HttpStatus status, String message) {
        return ResponseBody.<T>builder()
                .success(false)
                .statusCode(status.value())
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ResponseBody<T> error(int statusCode, String message) {
        return ResponseBody.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .message(message)
                .data(null)
                .build();
    }
}
