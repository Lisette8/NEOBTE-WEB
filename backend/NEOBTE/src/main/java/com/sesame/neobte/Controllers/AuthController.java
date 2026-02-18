package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.Responses.Auth.AuthResponse;
import com.sesame.neobte.DTO.Requests.Auth.LoginRequest;
import com.sesame.neobte.DTO.Requests.Auth.RegisterRequest;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
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
