package com.example.board.auth.service;

import com.example.board.auth.dto.request.MailMessage;

public interface EmailService {
    void send(MailMessage message);
}
