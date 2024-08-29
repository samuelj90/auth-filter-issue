package com.example.app.oauthserver.filter;

import com.example.app.oauthserver.manager.CustomAuthenticationManager;
import com.example.app.oauthserver.manager.CustomAuthenticationToken;
import com.example.app.oauthserver.model.TokenRequest;
import com.example.app.oauthserver.util.CustomOauthServerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@Slf4j
public class CustomAuthenticationFilter extends AuthenticationWebFilter {

    public CustomAuthenticationFilter(
            CustomAuthenticationManager authenticationManager,
            CustomOauthServerUtil util) {
        super(authenticationManager);
        setServerAuthenticationConverter(getServerAuthenticationConverter());
        setAuthenticationFailureHandler(authenticationFailureHandler(util));
        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/oauth/token"));
    }

    private TokenRequest getValidTokenRequest(MultiValueMap<String, String> formData) throws IllegalArgumentException {
        Map<String, String> map = formData.toSingleValueMap();
        ObjectMapper objectMapper = new ObjectMapper();
        TokenRequest tokenRequest = objectMapper.convertValue(map, TokenRequest.class);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<TokenRequest>> violations = validator.validate(tokenRequest);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " : " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(message);
        }
        return tokenRequest;
    }

    private ServerAuthenticationConverter getServerAuthenticationConverter() {
        return (ServerWebExchange exchange) -> exchange.getFormData().flatMap(formData -> {
            log.info("Executing Authentication converter");
            if (exchange.getRequest().getMethod().equals(GET)) {
                return Mono.error(new MethodNotAllowedException(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(), Collections.singleton(POST)));
            }
            try {
                return Mono.just(new CustomAuthenticationToken(getValidTokenRequest(formData)));
            } catch (IllegalArgumentException e) {
                log.error("Error occurred while converting to CustomAuthenticationToken", e);
                return Mono.error(new InsufficientAuthenticationException(e.getMessage(), e));
            } catch (Exception e) {
                log.error("Error occurred while converting to CustomAuthenticationToken", e);
                return Mono.error(new AuthenticationServiceException(INTERNAL_SERVER_ERROR.getReasonPhrase()));
            }
        });
    }

    private ServerAuthenticationFailureHandler authenticationFailureHandler(CustomOauthServerUtil util) {
        return (WebFilterExchange webFilterExchange, AuthenticationException exception) -> {
            if (exception instanceof AuthenticationServiceException) {
                return util.handleError(
                        INTERNAL_SERVER_ERROR,
                        new Exception(INTERNAL_SERVER_ERROR.getReasonPhrase()),
                        webFilterExchange.getExchange()
                );
            } else if (exception instanceof InsufficientAuthenticationException) {
                return util.handleError(
                        HttpStatus.BAD_REQUEST,
                        new Exception(exception.getMessage()),
                        webFilterExchange.getExchange()
                );
            }
            return util.handleError(UNAUTHORIZED, exception, webFilterExchange.getExchange());
        };
    }
}