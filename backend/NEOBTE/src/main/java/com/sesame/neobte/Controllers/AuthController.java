package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.AuthResponse;
import com.sesame.neobte.DTO.LoginRequest;
import com.sesame.neobte.DTO.RegisterRequest;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;


    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public Utilisateur register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
