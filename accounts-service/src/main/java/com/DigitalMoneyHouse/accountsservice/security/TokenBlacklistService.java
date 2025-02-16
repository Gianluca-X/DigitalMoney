package com.DigitalMoneyHouse.accountsservice.security;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
@Service
public class TokenBlacklistService {

        private final Set<String> blacklistedTokens = new HashSet<>();

        public void invalidateToken(String token) {
            blacklistedTokens.add(token);
            System.out.println(token);
        }

        public boolean isTokenInvalidated(String token) {
            return blacklistedTokens.contains(token);
        }
}


