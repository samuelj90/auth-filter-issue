package com.example.app.oauthserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.app.oauthserver.model.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class OauthserverApplicationTests {

	@Autowired
    private WebTestClient webTestClient;

	@Test
	void contextLoads() {
	}

	@Test
    @DisplayName("Should generate accessToken on valid request")
    void testOauthTokenEndpoint() {
        webTestClient.post().uri("/api/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue((tokenRequest("clientId", "password", "APP_CODE", "client_credentials")))
                .exchange()
                .expectStatus().isOk().expectBody(TokenResponse.class).value(tokenResponse -> {
                    assert tokenResponse.getAccessToken() != null;
                    assert tokenResponse.getAccessToken().split("\\.").length == 3;
                    assert tokenResponse.getExpiresIn() > 0;
                    assert tokenResponse.getTokenType().equals("Bearer");
                });
    }

	private MultiValueMap<String, String> tokenRequest(String clientId, String clientSecret, String appCode, String grantType) {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("clientId", clientId);
        tokenRequest.add("clientSecret", clientSecret);
        tokenRequest.add("appCode", appCode);
        tokenRequest.add("grantType", grantType);
        return tokenRequest;
    }

}
