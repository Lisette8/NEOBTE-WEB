package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Auth.AuthResponse;
import com.sesame.neobte.DTO.Requests.Auth.LoginRequest;
import com.sesame.neobte.DTO.Requests.Auth.RegisterRequest;
import com.sesame.neobte.Entities.Class.Utilisateur;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    void logout(HttpServletRequest request);
}
