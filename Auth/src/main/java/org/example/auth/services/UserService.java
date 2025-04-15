package org.example.auth.services;


import org.example.auth.dtos.LoginRequest;
import org.example.auth.dtos.RegisterRequest;
import org.example.auth.dtos.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public interface UserService {

    public UserResponse registerUser(RegisterRequest request);
    public Map<String, Object> login(LoginRequest request);
    public List<UserResponse> getAllUsers();
}
