package com.example.board.auth.service.impl;

import com.example.board.auth.service.JtiGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidJtiGenerator implements JtiGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
