package com.example.app.oauthserver.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TokenRequest", description = "Token request payload")
public class TokenRequest {
    @NotBlank
    @Schema(description = "Client Id")
    private String clientId;
    @NotBlank
    @Schema(description = "Client Secret")
    private String clientSecret;
    @NotBlank
    @Schema(description = "App Code")
    private String appCode;
    @NotBlank
    @Schema(description = "Grant Type", required = true)
    private String grantType;
}
