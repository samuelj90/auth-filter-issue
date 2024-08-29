package com.example.app.oauthserver.service;

import com.example.app.oauthserver.model.ClientDetailsWithMappings;
import reactor.core.publisher.Mono;

public interface UserDetailsService {
    Mono<ClientDetailsWithMappings> getClientDetailsWithMappings(String clientId, String appCode);
}
