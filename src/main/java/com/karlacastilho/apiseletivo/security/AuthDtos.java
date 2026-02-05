package com.karlacastilho.apiseletivo.security;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public static class RegisterRequest {
        @NotBlank public String username;
        @NotBlank public String password;
    }

    public static class LoginRequest {
        @NotBlank public String username;
        @NotBlank public String password;
    }

    public static class RefreshRequest {
        @NotBlank public String refreshToken;
    }

    public static class TokenResponse {
        public String accessToken;
        public String refreshToken;

        public TokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}