package com.example.board.auth.client;

import com.example.board.auth.dto.request.MemberProfileCreateRequest;
import com.example.board.auth.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-service", contextId = "memberApiClient")
public interface MemberApiClient {
    @PutMapping("/api/members/{member-id}/profile")
    ApiResponse<Void> createProfile(@PathVariable("member-id") Long id, @RequestBody MemberProfileCreateRequest request);

    @DeleteMapping("/api/members/{member-id}")
    ApiResponse<Void> deleteProfile(@PathVariable("member-id") Long id);
}
