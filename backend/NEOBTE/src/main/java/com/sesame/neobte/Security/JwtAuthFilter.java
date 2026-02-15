package com.sesame.neobte.Security;

import com.sesame.neobte.Security.Services.JwtService;
import com.sesame.neobte.Security.Services.TokenBlacklistService;
import jakarta.servlet.http.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import lombok.AllArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.util.*;
import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private JwtService jwtService;
    private TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = authHeader.substring(7);

        if (blacklistService.isBlacklisted(token)) {
            filterChain.doFilter(request, response);
            return;
        }


        try {
            Long userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));


                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
