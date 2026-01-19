package com.example.board.auth.utils;

import com.example.board.auth.dto.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class FeignExceptionUtils {
    private static final String NICKNAME_DUPLICATE_CODE = "MEMBER_PROFILE_E_002";
    private final JsonMapper jsonMapper;

    public boolean isDuplicateNickname(FeignException e) {
        return NICKNAME_DUPLICATE_CODE.equals(extractApiCode(e));
    }

    private String extractApiCode(FeignException e) {
        var body = e.contentUTF8();
        if(body == null || body.isBlank()) {
            return null;
        }
        var response = jsonMapper.readValue(body, new TypeReference<ApiResponse<Void>>() {});
        return response.code();
    }
}
