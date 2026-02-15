package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.AuthResponse;
import com.sesame.neobte.DTO.AuthRequests.LoginRequest;
import com.sesame.neobte.DTO.AuthRequests.RegisterRequest;
import com.sesame.neobte.Entities.Utilisateur;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    Utilisateur register(RegisterRequest request);
    void logout(HttpServletRequest request);
}
