package com.example.board.auth.config;

import com.example.board.auth.exception.RetryableRemoteException;
import com.example.board.auth.retry.CreateProfileRetryListener;
import com.example.board.auth.retry.DeleteProfileRetryListener;
import com.example.board.auth.retry.RetryMethod;
import com.example.board.auth.retry.RetryableMethodName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class RetryConfig {
    @Bean
    @RetryMethod(RetryableMethodName.CREATE_PROFILE)
    public RetryTemplate createProfileRetryTemplate(CreateProfileRetryListener retryListener) {
        var retryPolicy = defaultProfilePolicy();

        var retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(retryListener);
        return retryTemplate;
    }

    @Bean
    @RetryMethod(RetryableMethodName.DELETE_PROFILE)
    public RetryTemplate deleteProfileRetryTemplate(DeleteProfileRetryListener retryListener) {
        var retryPolicy = defaultProfilePolicy();

        var retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(retryListener);
        return retryTemplate;
    }

    @Bean
    public Map<RetryableMethodName, RetryTemplate> retryTemplateMap(
            @RetryMethod(RetryableMethodName.CREATE_PROFILE) RetryTemplate createProfile,
            @RetryMethod(RetryableMethodName.DELETE_PROFILE) RetryTemplate deleteProfile
    ) {
        var map = new EnumMap<RetryableMethodName, RetryTemplate>(RetryableMethodName.class);
        map.put(RetryableMethodName.CREATE_PROFILE, createProfile);
        map.put(RetryableMethodName.DELETE_PROFILE, deleteProfile);

        return map;
    }

    private RetryPolicy defaultProfilePolicy() {
        return RetryPolicy.builder()
                .maxRetries(3)
                .delay(Duration.ofMillis(100))
                .multiplier(1.5)
                .maxDelay(Duration.ofMillis(500))
                .jitter(Duration.ofMillis(50))
                .timeout(Duration.ofSeconds(1))
                .includes(RetryableRemoteException.class)
                .build();
    }
}
