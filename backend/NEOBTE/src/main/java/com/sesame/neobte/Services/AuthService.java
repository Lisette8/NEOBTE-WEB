package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.AuthResponse;
import com.sesame.neobte.DTO.LoginRequest;
import com.sesame.neobte.DTO.RegisterRequest;
import com.sesame.neobte.Entities.Client;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    Client register(RegisterRequest request);
    void logout();
}
