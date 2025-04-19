package com.dm.adaptive.webservice.service;

public interface UserWebService {
    String getUser(Long id);
    String createUser(String userPayload);
    String getAllUsers();
}
