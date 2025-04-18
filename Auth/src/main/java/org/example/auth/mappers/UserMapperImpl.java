package org.example.auth.mappers;

import org.example.auth.dtos.RegisterRequest;
import org.example.auth.dtos.UserResponse;
import org.example.auth.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public User fromRegisterRequest(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        return user;
    }

    @Override
    public UserResponse toUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUsername(user.getUsername());
        userResponse.setEmail(user.getEmail());
        userResponse.setId(user.getId());
        userResponse.setNom(user.getNom());
        userResponse.setPrenom(user.getPrenom());
        return userResponse;
    }
}
