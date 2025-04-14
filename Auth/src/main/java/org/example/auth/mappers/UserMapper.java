package org.example.auth.mappers;

import org.example.auth.dtos.RegisterRequest;
import org.example.auth.dtos.UserResponse;
import org.example.auth.entities.User;

public interface UserMapper {

    public User fromRegisterRequest(RegisterRequest request);
    public UserResponse toUserResponse(User user);

}
