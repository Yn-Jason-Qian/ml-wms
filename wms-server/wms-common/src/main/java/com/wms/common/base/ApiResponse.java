package com.wms.common.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;

    // ---- Success ----

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data, UUID.randomUUID().toString().substring(0, 8));
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    // ---- Client errors ----

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null, UUID.randomUUID().toString().substring(0, 8));
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, null, UUID.randomUUID().toString().substring(0, 8));
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message, null, UUID.randomUUID().toString().substring(0, 8));
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null, UUID.randomUUID().toString().substring(0, 8));
    }

    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409, message, null, UUID.randomUUID().toString().substring(0, 8));
    }

    // ---- Server error ----

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null, UUID.randomUUID().toString().substring(0, 8));
    }

    // ---- Builder style ----

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, UUID.randomUUID().toString().substring(0, 8));
    }
}
