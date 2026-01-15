package com.example.board.auth.dto.request;

import lombok.Builder;

@Builder
public record MailMessage(String to, String subject, String text) {
}
