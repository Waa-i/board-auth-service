package com.example.board.auth.retry;

import lombok.RequiredArgsConstructor;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RetryTemplateRegistry {
    private final Map<RetryableMethodName, RetryTemplate> retryTemplateMap;

    public RetryTemplate getRetryTemplate(RetryableMethodName methodName) {
        return retryTemplateMap.get(methodName);
    }
}
