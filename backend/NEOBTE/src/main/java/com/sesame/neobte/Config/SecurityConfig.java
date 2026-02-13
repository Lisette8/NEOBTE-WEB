package com.sesame.neobte.Config;


import com.sesame.neobte.Entities.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable) // disable csrf for Postman
                .authorizeHttpRequests(auth -> auth

                        // v1 permit all for quicker testing via postman
                        .requestMatchers("/api/client/createClient").permitAll()
                        .requestMatchers("/api/client/all").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // protected
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



}
