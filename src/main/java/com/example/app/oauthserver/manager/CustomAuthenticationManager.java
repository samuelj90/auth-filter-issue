package com.example.app.oauthserver.manager;

import com.example.app.oauthserver.model.ClientDetailsWithMappings;
import com.example.app.oauthserver.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationManager implements ReactiveAuthenticationManager{
    private final UserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication instanceof CustomAuthenticationToken customAuthenticationToken) {
            try {
                return userDetailsService.getClientDetailsWithMappings(
                                customAuthenticationToken.getClientId(),
                                customAuthenticationToken.getAppCode()
                        ).
                        filter(userDetails -> passwordEncoder().matches(
                                customAuthenticationToken.getClientSecret(),
                                userDetails.getOauthClientDetails().getClientSecret()
                        )).
                        switchIfEmpty(Mono.error(new BadCredentialsException("Invalid client credentials"))).
                        flatMap((ClientDetailsWithMappings clientDetails) -> {
                            if (Arrays.stream(
                                            clientDetails.
                                                    getOauthClientDetails().
                                                    getAuthorizedGrantTypes().
                                                    split(",")
                                    ).
                                    noneMatch(grantType -> grantType.
                                            equals(customAuthenticationToken.getGrantType()
                                            )
                                    )
                            ) {
                                return Mono.error(new BadCredentialsException("Invalid Grant Type"));
                            }
                            CustomAuthenticationToken authenticatedToken = new CustomAuthenticationToken(
                                    clientDetails.getOauthClientDetails().getClientId(),
                                    clientDetails.getOauthClientDetails().getClientSecret(),
                                    customAuthenticationToken.getGrantType(),
                                    customAuthenticationToken.getAppCode(),
                                    Arrays.stream(clientDetails.
                                                    getOauthClientDetails().
                                                    getAuthorities().
                                                    split(",")).
                                            map(SimpleGrantedAuthority::new).
                                            collect(Collectors.toList()),
                                    clientDetails.getOauthClientDetails().getAccessTokenValidity(),
                                    true
                            );
                            SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
                            return Mono.just(authenticatedToken);
                        });
            } catch (Exception e) {
                log.error("Error occurred while authenticating", e);
                return Mono.error(new AuthenticationServiceException("Some error occurred while authenticating"));
            }
        }
        return Mono.error(new BadCredentialsException("Invalid Authentication Details"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}