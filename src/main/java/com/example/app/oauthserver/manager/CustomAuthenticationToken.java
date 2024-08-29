package com.example.app.oauthserver.manager;

import com.example.app.oauthserver.model.TokenRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Setter
public class CustomAuthenticationToken extends AbstractAuthenticationToken {
    private final String clientId;
    private final String clientSecret;
    private final String appCode;
    private final String grantType;
    private int expirySeconds;

    public CustomAuthenticationToken(String clientId,
                                  String clientSecret,
                                  String grantType,
                                  String appCode,
                                  Collection<GrantedAuthority> authorities,
                                  int expirySeconds,
                                  boolean isAuthenticated
    ) {
        super(authorities);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.appCode = appCode;
        this.expirySeconds = expirySeconds;
        super.setAuthenticated(isAuthenticated);
    }

    public CustomAuthenticationToken(TokenRequest tokenRequest) {
        super(null);
        this.clientId = tokenRequest.getClientId();
        this.clientSecret = tokenRequest.getClientSecret();
        this.grantType = tokenRequest.getGrantType();
        this.appCode = tokenRequest.getAppCode();
        this.expirySeconds = 0;
        super.setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return clientSecret;
    }

    @Override
    public Object getPrincipal() {
        return clientId;
    }
}