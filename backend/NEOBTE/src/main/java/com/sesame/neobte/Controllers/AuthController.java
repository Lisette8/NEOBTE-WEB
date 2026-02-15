package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.AuthResponse;
import com.sesame.neobte.DTO.LoginRequest;
import com.sesame.neobte.DTO.RegisterRequest;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public Utilisateur register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        authService.logout(request);
        return "Logged out successfully";
    }

}
