package com.dm.adaptive.webservice.service;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserWebServiceImpl implements UserWebService {

    private final RestTemplate restTemplate;
    private static final String USER_SERVICE_URL = "http://localhost:8999/auth";

    public UserWebServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String extractAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
        }
        return "";
    }

    @Override
    public String getUser(Long id) {
        String accessToken = extractAccessToken();
        System.out.println("Forwarding token: " + accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String targetUrl = USER_SERVICE_URL + "/" + id;
        ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        return response.getBody();
    }

    @Override
    public String createUser(String userPayload) {
        String accessToken = extractAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(userPayload, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                USER_SERVICE_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        return response.getBody();
    }

    @Override
    public String getAllUsers() {
        String accessToken = extractAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                USER_SERVICE_URL,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        return response.getBody();
    }
}
