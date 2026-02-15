package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.AuthResponse;
import com.sesame.neobte.DTO.LoginRequest;
import com.sesame.neobte.DTO.RegisterRequest;
import com.sesame.neobte.Entities.Utilisateur;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    Utilisateur register(RegisterRequest request);
    void logout();
}
