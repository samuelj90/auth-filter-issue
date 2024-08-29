package com.example.app.oauthserver.config;

import com.example.app.oauthserver.filter.CustomAuthenticationFilter;
import com.example.app.oauthserver.util.CustomOauthServerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationWebFilter;
    private final CustomOauthServerUtil util;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange(customizer -> customizer.
                        pathMatchers(HttpMethod.GET, "/actuator/info").permitAll().
                        pathMatchers(HttpMethod.GET, "/actuator/health").permitAll().
                        pathMatchers(HttpMethod.POST, "/actuator/refresh").permitAll().
                        pathMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll().
                        pathMatchers(HttpMethod.GET, "/v3/api-docs").permitAll().
                        pathMatchers(HttpMethod.GET, "/v3/api-docs.yaml").permitAll().
                        pathMatchers(HttpMethod.GET, "/v3/api-docs/swagger-config").permitAll().
                        pathMatchers(HttpMethod.GET, "/webjars/swagger-ui/**").permitAll().
                        pathMatchers(HttpMethod.POST, "/api/oauth/token").hasAnyAuthority("ROLE_UI", "ROLE_BE").
                        anyExchange().denyAll()
                ).
                csrf(ServerHttpSecurity.CsrfSpec::disable).
                addFilterAt(customAuthenticationWebFilter, SecurityWebFiltersOrder.FORM_LOGIN).
                exceptionHandling(customizer -> customizer.
                        authenticationEntryPoint(
                                (ServerWebExchange exchange, AuthenticationException ex) ->
                                        util.handleError(HttpStatus.UNAUTHORIZED, ex, exchange)
                        ).
                        accessDeniedHandler(
                                (ServerWebExchange exchange, AccessDeniedException ex) ->
                                        util.handleError(HttpStatus.FORBIDDEN, ex, exchange)
                        )
                ).build();
    }
}