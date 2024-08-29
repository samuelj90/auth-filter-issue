package com.example.app.oauthserver.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("OAUTH_CLIENT_DETAILS")
public class OauthClientDetails {
    @Id
    private String clientId;
    private String clientSecret;
    private String authorities;
    private Integer accessTokenValidity;
    private String authorizedGrantTypes;
}