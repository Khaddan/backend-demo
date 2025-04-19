package com.dm.adaptive.webservice.controller;


import com.dm.adaptive.webservice.service.UserWebService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webapp")
public class WebController {

    private final UserWebService userWebService;

    public WebController(UserWebService userWebService) {
        this.userWebService = userWebService;
    }

    @GetMapping("/auth/current")
    public ResponseEntity<String> GetUser(@PathVariable Long id) {
        String result = userWebService.getUser(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<String> CreateUser(@RequestBody String userPayload) {
        String result = userWebService.createUser(userPayload);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<String> GetUsers() {
        String result = userWebService.getAllUsers();
        return ResponseEntity.ok(result);
    }


}
