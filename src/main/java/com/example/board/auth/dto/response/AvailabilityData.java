package com.example.board.auth.dto.response;

public record AvailabilityData(boolean available, String reason) {
    public static AvailabilityData ok(String reason) {
        return new AvailabilityData(true, reason);
    }
    public static AvailabilityData no(String reason) {
        return new AvailabilityData(false, reason);
    }
}
