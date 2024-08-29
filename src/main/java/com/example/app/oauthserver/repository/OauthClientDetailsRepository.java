package com.example.app.oauthserver.repository;


import com.example.app.oauthserver.entity.OauthClientDetails;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OauthClientDetailsRepository
        extends ReactiveCrudRepository<OauthClientDetails, String> {
}