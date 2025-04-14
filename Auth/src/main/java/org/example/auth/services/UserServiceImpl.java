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

//    @Override
//    public UserResponse registerUser(RegisterRequest request) {
//
//        // 1️⃣ Créer l'utilisateur dans Keycloak
//        UsersResource usersResource = keycloak.realm(realm).users();
//        UserRepresentation kcUser = new UserRepresentation();
//        kcUser.setUsername(request.getUsername());
//        kcUser.setEmail(request.getEmail());
//        kcUser.setFirstName(request.getNom());
//        kcUser.setEnabled(true);
//        //Response response = usersResource.create(kcUser);
//        if (response.getStatus() != 201) {
//            throw new RuntimeException("Erreur Keycloak: " + response.getStatus());
//        }
//        String keycloakId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
//
//        // 2️⃣ Définir mot de passe
//        CredentialRepresentation password = new CredentialRepresentation();
//        password.setTemporary(false);
//        password.setType(CredentialRepresentation.PASSWORD);
//        password.setValue(request.getPassword());
//        usersResource.get(keycloakId).resetPassword(password);
//
//        // 3️⃣ Attribuer le rôle
//        RoleRepresentation role = keycloak.realm(realm)
//                .roles()
//                .get(String.valueOf(request.getRole()))
//                .toRepresentation();
//
//        usersResource.get(keycloakId)
//                .roles()
//                .realmLevel()
//                .add(Collections.singletonList(role));
//
//        // 4️⃣ Sauvegarder dans la base
//        User user = userMapper.fromRegisterRequest(request);
//        user.setKeycloakId(keycloakId);
//        User savedUser = userRepository.save(user);
//
//        return userMapper.toUserResponse(savedUser);
//    }

    @Override
    public UserResponse registerUser(RegisterRequest request) {
        return null;
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
