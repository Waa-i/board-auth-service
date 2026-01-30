package com.example.board.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OAuth2RegisteredClientRequest(
        @NotBlank(message = "애플리케이션 이름은 필수입니다.")
        @Size(min = 1, max = 20, message = "애플리케이션 이름은 20자 이내입니다.")
        String clientName,
        @NotBlank(message = "Client Secret은 필수입니다.")
        @Size(min = 10, max = 20, message = "Client Secret은 10~20자 사이의 영문, 숫자만 사용 가능합니다.")
        String clientSecret,
        @NotBlank(message = "Callback URL은 필수입니다.")
        String redirectUris
) {
    public static OAuth2RegisteredClientRequest empty() {
        return new OAuth2RegisteredClientRequest(null, null, null);
    }
}
