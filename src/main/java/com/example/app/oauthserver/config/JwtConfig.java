package com.example.app.oauthserver.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RefreshScope
@RequiredArgsConstructor
public class JwtConfig {
    private final JwtConfigProperties config;

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        KeyStore keyStore = loadKeyStore();
        RSAPrivateKey privateKey = getPrivateKey(keyStore);
        RSAPublicKey publicKey = getPublicKey(keyStore);

        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();
        JWKSet jwkSet = new JWKSet(jwk);

        return new NimbusJwtEncoder(new ImmutableJWKSet<>(jwkSet));
    }

    private KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(config.getKeyStoreType());
        keyStore.load(config.getKeyStore().getInputStream(), config.getKeyStorePassword().toCharArray());
        return keyStore;
    }

    private RSAPrivateKey getPrivateKey(KeyStore keyStore) throws Exception {
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(config.getKeyAlias(), config.getKeyPassword().toCharArray());
        if (privateKey == null) {
            throw new IllegalStateException("Private key not found in keystore");
        }
        return (RSAPrivateKey) privateKey;
    }

    private RSAPublicKey getPublicKey(KeyStore keyStore) throws Exception {
        Certificate certificate = keyStore.getCertificate(config.getKeyAlias());
        if (certificate == null) {
            throw new IllegalStateException("Certificate not found in keystore");
        }
        return (RSAPublicKey) certificate.getPublicKey();
    }
}

@Component
@ConfigurationProperties(prefix = "jwt")
@RefreshScope
@Getter
@Setter
class JwtConfigProperties {
    private String keyStoreType;
    private Resource keyStore;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;
}