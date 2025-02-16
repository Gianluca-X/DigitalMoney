package com.example.userservice.service.client;

import com.example.userservice.dto.entry.AccountCreationRequest;
import com.example.userservice.dto.entry.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
@FeignClient(name = "accounts-service",url= "http://localhost:8084")
public interface AccountClient {
    @PostMapping("/accounts/create")
    AccountResponse createAccount(AccountCreationRequest request);
}
