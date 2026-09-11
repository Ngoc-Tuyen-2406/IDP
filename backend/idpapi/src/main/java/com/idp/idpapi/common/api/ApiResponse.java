package com.idp.idpapi.common.api;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<ApiError> errors,
        Instant timestamp) {

    public ApiResponse {
        errors = errors == null ? Collections.emptyList() : List.copyOf(errors);
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Collections.emptyList(), Instant.now());
    }

    public static ApiResponse<Void> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> failure(String message, List<ApiError> errors) {
        return new ApiResponse<>(false, message, null, errors, Instant.now());
    }
}
