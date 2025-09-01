package com.example.userservice.service.client;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.entry.UserRegisterAuthRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service",url = "http://localhost:8082")
public interface AuthClient {
    @PostMapping("/auth/register")
    AuthResponse registerUser(@RequestBody UserRegisterAuthRequest request);
}
