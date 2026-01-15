package com.example.board.auth.dto.response;

public record ApiResponse<T>(Boolean success, String code, String message, T data) {
    public static <T> ApiResponse<T> success(ApiCode success) {
        return new ApiResponse<>(true, success.getCode(), success.getMessage(), null);
    }
    public static <T> ApiResponse<T> success(ApiCode success, T data) {
        return new ApiResponse<>(true, success.getCode(), success.getMessage(), data);
    }
    public static <T> ApiResponse<T> error(ApiCode error) {
        return new ApiResponse<>(false, error.getCode(), error.getMessage(), null);
    }
}