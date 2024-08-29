package com.example.app.oauthserver.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(name = "ErrorResponse", description = "Error Response")
public class ErrorResponse {
    @Schema(description = "HTTP Status Code")
    private int status;
    @Schema(description = "Error Code")
    private String error;
    @Schema(description = "Error Message")
    private String message;
    @Schema(description = "Request Id")
    private String requestId;
}