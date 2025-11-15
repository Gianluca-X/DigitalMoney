package com.example.userservice.service.client;

import com.example.userservice.config.FeignClientConfig;
import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.entry.AccountCreationRequest;
import com.example.userservice.dto.entry.AccountResponse;
import com.example.userservice.dto.entry.UserUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "accounts-service", configuration = FeignClientConfig.class)
public interface AccountClient {

    @PostMapping("/accounts/create")
    AccountResponse createAccount(
            @RequestBody AccountCreationRequest request
    );

    @DeleteMapping("/accounts/{accountId}")
    AccountResponse deleteAccount(@PathVariable Long accountId);

}


