package org.example.auth.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.auth.dtos.LoginRequest;
import org.example.auth.dtos.RegisterRequest;
import org.example.auth.dtos.UserResponse;
import org.example.auth.entities.User;
import org.example.auth.mappers.UserMapperImpl;
import org.example.auth.repositorys.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapperImpl userMapper;




    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Override
    public UserResponse registerUser(RegisterRequest request) {
        try {
            UserRepresentation kcUser = new UserRepresentation();
            kcUser.setUsername(request.getUsername());
            kcUser.setEmail(request.getEmail());
            kcUser.setFirstName(request.getPrenom());
            kcUser.setLastName(request.getNom());
            kcUser.setEnabled(true);

            ObjectMapper objectMapper = new ObjectMapper();
            String userJson = objectMapper.writeValueAsString(kcUser);

            String token = keycloak.tokenManager().getAccessToken().getToken();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/admin/realms/" + realm + "/users"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(userJson))
                    .build();

            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            String keycloakId;

            if (httpResponse.statusCode() == 201) {
                String locationHeader = httpResponse.headers()
                        .firstValue("Location")
                        .orElseThrow(() -> new RuntimeException("User created but no Location header"));
                keycloakId = locationHeader.replaceAll(".*/([^/]+)$", "$1");
            } else if (httpResponse.statusCode() == 409) {
                throw new RuntimeException("User already exists.");
            } else if (httpResponse.statusCode() == 403) {
                System.out.println("⚠️ 403 Forbidden — fallback to user search");
                List<UserRepresentation> users = keycloak.realm(realm).users().search(request.getUsername());
                // Ajoutez un log pour afficher les en-têtes de la requête
                //System.out.println("Headers envoyés : " + clientInvocation.getHeaders());
                if (users.isEmpty()) {
                    throw new RuntimeException("User possibly created but not found.");
                }
                keycloakId = users.get(0).getId();
            } else {
                throw new RuntimeException("Keycloak user creation failed: " + httpResponse.statusCode());
            }

            CredentialRepresentation password = new CredentialRepresentation();
            password.setTemporary(false);
            password.setType(CredentialRepresentation.PASSWORD);
            password.setValue(request.getPassword());

            keycloak.realm(realm).users().get(keycloakId).resetPassword(password);

            User user = userMapper.fromRegisterRequest(request);
            user.setKeycloakId(keycloakId);
            User savedUser = userRepository.save(user);

            return userMapper.toUserResponse(savedUser);

        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        try {
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            Map<String, String> formData = Map.of(
                    "grant_type", "password",
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "username", request.getUsername(),
                    "password", request.getPassword()
            );

            String encodedForm = formData.entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(encodedForm))
                    .build();

            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException("Login failed: " + httpResponse.statusCode() + " ➜ " + httpResponse.body());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(httpResponse.body(), Map.class); // contains access_token, etc.

        } catch (Exception e) {
            throw new RuntimeException("Login error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserResponse> getAllUsers() {
        try {
            String token = keycloak.tokenManager().getAccessToken().getToken();

            String url = serverUrl + "/admin/realms/" + realm + "/users";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Keycloak error: " + response.statusCode() + " ➜ " + response.body());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            UserRepresentation[] users = objectMapper.readValue(response.body(), UserRepresentation[].class);

            return Arrays.stream(users)
                    .map(user -> {
                        UserResponse dto = new UserResponse();
                        dto.setUsername(user.getUsername());
                        dto.setEmail(user.getEmail());
                        dto.setNom(user.getFirstName());
                        dto.setPrenom(user.getLastName());
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs: " + e.getMessage(), e);
        }
    }

    @Override
    public UserResponse getCurrentUser(Jwt jwt)  {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(username);
        dto.setEmail(email);
        return dto;
    }



}
