package com.example.userservice.service.client;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.entry.UserRegisterAuthRequest;
import com.example.userservice.dto.entry.UserUpdateRequest;
import com.example.userservice.entity.Role;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service",url = "http://localhost:8082")
public interface AuthClient {
    @PostMapping("/auth/register")
    AuthResponse registerUser(@RequestBody UserRegisterAuthRequest request);
    @PutMapping("/auth/update")
    AuthResponse updateUserAuth(@RequestBody UserUpdateRequest request);
    @DeleteMapping("/auth/delete/{authId}")
    String deleteUserAuth(@PathVariable Long authId);



}
