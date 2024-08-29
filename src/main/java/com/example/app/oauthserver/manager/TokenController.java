package com.example.app.oauthserver.manager;

import com.example.app.oauthserver.model.ErrorResponse;
import com.example.app.oauthserver.model.TokenRequest;
import com.example.app.oauthserver.model.TokenResponse;
import com.example.app.oauthserver.util.CustomOauthServerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class TokenController {

    private final CustomOauthServerUtil util;

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Generate token", description = "Generates a new token for the given request")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token Response",
                    content = @Content(
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "405",
                    description = "Method Not Allowed",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public Mono<TokenResponse> getToken(@ModelAttribute
                                        @Parameter(description = "Request to generate a new token", required = true) TokenRequest tokenRequest) {
        log.info("Token request received for AppCode: {}", tokenRequest.getAppCode());
        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();
        if (authenticationToken instanceof CustomAuthenticationToken token) {
            if (token.getClientId().equals(tokenRequest.getClientId()) &&
                    token.getGrantType().equals(tokenRequest.getGrantType())) {
                return util.getTokenResponse(token);
            }
            log.error("ClientId and GrantType mismatch for AppCode: {}", tokenRequest.getAppCode());
        }
        return Mono.error(new BadCredentialsException("Invalid Authentication Details"));

    }
}