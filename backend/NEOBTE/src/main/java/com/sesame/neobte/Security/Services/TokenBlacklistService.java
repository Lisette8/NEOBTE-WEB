package com.sesame.neobte.Security.Services;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Store token and expiry time instead of just a Set
    // This allows to automatically clean up expired tokens
    private final Map<String, Date> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Date expiryDate) {
        cleanExpiredTokens();
        blacklist.put(token, expiryDate);
    }

    public boolean isBlacklisted(String token) {
        cleanExpiredTokens();
        return blacklist.containsKey(token);
    }

    // Remove tokens that have already expired
    private void cleanExpiredTokens() {
        Date now = new Date();
        Iterator<Map.Entry<String, Date>> iterator = blacklist.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Date> entry = iterator.next();
            if (entry.getValue().before(now)) {
                iterator.remove();
            }
        }
    }
}
