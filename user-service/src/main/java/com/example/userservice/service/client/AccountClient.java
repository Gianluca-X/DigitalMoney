package com.example.userservice.service.client;

import com.example.userservice.config.FeignClientConfig;
import com.example.userservice.dto.entry.AccountCreationRequest;
import com.example.userservice.dto.entry.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
@FeignClient(name = "accounts-service", url = "http://accounts-service:8084", configuration = FeignClientConfig.class)
public interface AccountClient {

    @PostMapping("/accounts/create")
    AccountResponse createAccount(
            @RequestBody AccountCreationRequest request,
            @RequestHeader("Authorization") String token
    );
}


