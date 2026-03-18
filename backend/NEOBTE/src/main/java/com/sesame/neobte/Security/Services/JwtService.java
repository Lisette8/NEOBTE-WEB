package com.sesame.neobte.Security.Services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.*;

import java.security.*;
import java.util.*;
import java.util.function.Function;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La clé JWT (jwt.secret) est manquante. Définissez la variable d'environnement JWT_SECRET.");
        }
        // HS256 requires >= 256 bits (32 bytes) key length.
        if (secret.getBytes().length < 32) {
            throw new IllegalStateException("La clé JWT est trop courte. Utilisez au moins 32 caractères pour JWT_SECRET.");
        }
    }


    //token generation
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private Key getSignKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public Long extractUserId(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }


    //I used T to return any data type the function has to return...
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    //is token valid methods (Boolean)
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public boolean isTokenValid(String token, Long userId) {
        final Long extractedUserId = extractUserId(token);
        return extractedUserId.equals(userId) && !isTokenExpired(token);
    }

}
