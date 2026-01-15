package com.example.board.auth.utils;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class EmailDomainPolicy {
    private static final Set<String> ALLOWED_DOMAIN = Set.of("gmail.com", "naver.com");

    public boolean isDomainAllowed(String email) {
        if(email == null) return false;

        var normEmail = normalize(email);
        var at = normEmail.lastIndexOf("@");
        if(at <= 0 || at == normEmail.length() - 1) return false;

        var domain = normEmail.substring(at + 1);
        return ALLOWED_DOMAIN.contains(domain);
    }

    private String normalize(String str) {
        return str.strip().toLowerCase(Locale.ROOT);
    }
}
