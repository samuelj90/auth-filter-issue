package com.example.app.oauthserver.util;

import com.example.app.oauthserver.manager.CustomAuthenticationToken;
import com.example.app.oauthserver.model.ErrorResponse;
import com.example.app.oauthserver.model.TokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOauthServerUtil {

    private final JwtEncoder jwtEncoder;

    private static ErrorResponse getErrorResponse(HttpStatus status, String message, ServerWebExchange exchange) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(message);
        errorResponse.setRequestId(exchange.getRequest().getId());
        return errorResponse;
    }

    public ResponseEntity<ErrorResponse> handleException(HttpStatus status, Exception exception, ServerWebExchange exchange) {
        log.error("Error occurred: status : {} requestId: {}", status, exchange.getRequest().getId(), exception);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse errorResponse = getErrorResponse(status, exception.getMessage(), exchange);
        return ResponseEntity.status(status).body(errorResponse);
    }

    public Mono<Void> handleError(HttpStatus status, Exception exception, ServerWebExchange exchange) {
        ResponseEntity<ErrorResponse> responseEntity = handleException(status, exception, exchange);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            exchange.getResponse().setStatusCode(responseEntity.getStatusCode());
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                    .bufferFactory().wrap(new ObjectMapper().writeValueAsBytes(responseEntity.getBody()))));
        } catch (JsonProcessingException e) {
            log.error("Error occurred while writing response", e);
            return exchange.getResponse().setComplete();
        }
    }

    public Mono<TokenResponse> getTokenResponse(CustomAuthenticationToken token) {
        String jwtToken = generateJWTToken(token);
        log.info("Token generated successfully for AppCode: {}", token.getAppCode());
        return Mono.just(new TokenResponse(jwtToken, token.getExpirySeconds(), "Bearer"));
    }

    private String generateJWTToken(CustomAuthenticationToken token) {
        log.info("Generating JWT token for AppCode: {}", token.getAppCode());
        JwtClaimsSet.Builder builder = JwtClaimsSet.
                builder().
                subject(token.getClientId()).
                issuer("Custom OAUTH SERVER").
                claim("authorities", token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()).
                claim("appCode", token.getAppCode()).
                issuedAt(Instant.now()).
                expiresAt(Instant.now().plusSeconds(token.getExpirySeconds()));
        JwtEncoderParameters params = JwtEncoderParameters.from(builder.build());
        return jwtEncoder.encode(params).getTokenValue();
    }
}