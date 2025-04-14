package org.example.auth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import org.example.auth.dtos.LoginRequest;
import org.example.auth.dtos.RegisterRequest;
import org.example.auth.dtos.UserResponse;
import org.example.auth.entities.User;
import org.example.auth.mappers.UserMapperImpl;
import org.example.auth.repositorys.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            // 1️⃣ Create JSON payload for Keycloak user
            UserRepresentation kcUser = new UserRepresentation();
            kcUser.setUsername(request.getUsername());
            kcUser.setEmail(request.getEmail());
            kcUser.setEnabled(true);

            ObjectMapper objectMapper = new ObjectMapper();
            String userJson = objectMapper.writeValueAsString(kcUser);

            // 2️⃣ Get access token from Keycloak
            String token = keycloak.tokenManager().getAccessToken().getToken();

            // 3️⃣ Send POST request to Keycloak to create user
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/admin/realms/" + realm + "/users"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(userJson))
                    .build();

            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            String keycloakId;

            // 4️⃣ Get user ID from response or fallback
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
                if (users.isEmpty()) {
                    throw new RuntimeException("User possibly created but not found.");
                }
                keycloakId = users.get(0).getId();
            } else {
                throw new RuntimeException("Keycloak user creation failed: " + httpResponse.statusCode());
            }

            // 5️⃣ Set password
            CredentialRepresentation password = new CredentialRepresentation();
            password.setTemporary(false);
            password.setType(CredentialRepresentation.PASSWORD);
            password.setValue(request.getPassword());

            keycloak.realm(realm).users().get(keycloakId).resetPassword(password);

            // 6️⃣ Save to local DB
            User user = userMapper.fromRegisterRequest(request);
            user.setKeycloakId(keycloakId);
            User savedUser = userRepository.save(user);

            // 7️⃣ Return mapped response
            return userMapper.toUserResponse(savedUser);

        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }



    @Override
    public Map<String, Object> login(LoginRequest request) {
        try {
            // 1️⃣ Build the token URL
            String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            // 2️⃣ Prepare form data
            Map<String, String> formData = Map.of(
                    "grant_type", "password",
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "username", request.getUsername(),
                    "password", request.getPassword()
            );

            // 3️⃣ Encode form as x-www-form-urlencoded
            String encodedForm = formData.entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            // 4️⃣ Create HTTP request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(encodedForm))
                    .build();

            // 5️⃣ Send request
            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new RuntimeException("Login failed: " + httpResponse.statusCode() + " ➜ " + httpResponse.body());
            }

            // 6️⃣ Parse JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(httpResponse.body(), Map.class); // contains access_token, etc.

        } catch (Exception e) {
            throw new RuntimeException("Login error: " + e.getMessage(), e);
        }
    }


    @Override
    public UserResponse getCurrentUser(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        return userMapper.toUserResponse(userOpt.get());
    }
}
