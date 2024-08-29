package com.example.app.oauthserver.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TokenResponse", description = "Token Response")
public class TokenResponse {
    @Schema(description = "Access Token")
    private String accessToken;
    @Schema(description = "Token Expiry in seconds")
    private int expiresIn;
    @Schema(description = "Token Type")
    private String tokenType;
}