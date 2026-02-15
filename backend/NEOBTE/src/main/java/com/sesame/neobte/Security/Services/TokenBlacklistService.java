package com.sesame.neobte.Security.Services;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklist = new HashSet<>();



    public void blacklistToken(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
