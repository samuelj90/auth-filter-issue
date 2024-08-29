package com.example.app.oauthserver.service;

import com.example.app.oauthserver.entity.OauthClientDetails;
import com.example.app.oauthserver.model.ClientDetailsWithMappings;
import com.example.app.oauthserver.repository.OauthClientDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final OauthClientDetailsRepository oauthClientDetailsRepository;

    @Override
    public Mono<ClientDetailsWithMappings> getClientDetailsWithMappings(String clientId, String appCode) {
        try {
            Mono<OauthClientDetails> clientDetailsMono = oauthClientDetailsRepository.findById(clientId)
                    .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid client credentials")));
            return clientDetailsMono.flatMap((OauthClientDetails obj) -> {
                ClientDetailsWithMappings mapping= new ClientDetailsWithMappings();
                mapping.setOauthClientDetails(obj);
                return Mono.just(mapping);
            });
        } catch (Exception e) {
            log.error("Error in getClientDetailsWithMappings", e);
            throw new AuthorizationServiceException(INTERNAL_SERVER_ERROR.getReasonPhrase());
        }
    }
}