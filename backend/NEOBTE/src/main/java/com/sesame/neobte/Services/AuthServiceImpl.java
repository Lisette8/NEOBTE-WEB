package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Auth.AuthResponse;
import com.sesame.neobte.DTO.Requests.Auth.LoginRequest;
import com.sesame.neobte.DTO.Requests.Auth.RegisterRequest;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.UnauthorizedException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Security.Services.JwtService;
import com.sesame.neobte.Security.Services.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private IUtilisateurRepository clientRepository;
    private PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;


    @Override
    public AuthResponse login(LoginRequest request) {

        Utilisateur utilisateur = clientRepository.findByEmail(request.getEmail());

        if (utilisateur == null) {
            throw new UnauthorizedException("Invalid email or password");
        }

        boolean ifTrueMDP = passwordEncoder.matches(
                request.getMotDePasse(),
                utilisateur.getMotDePasse()
        );

        if (!ifTrueMDP) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                utilisateur.getIdUtilisateur(),
                utilisateur.getRole().toString()
        );

        return new AuthResponse(token);
    }


    @Override
    public AuthResponse register(RegisterRequest request) {

        if (clientRepository.findByEmail(request.getEmail()) != null) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists");
        }

        if (clientRepository.existsByTelephone(request.getTelephone())) {
            throw new BadRequestException("This phone number is already linked to an account");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(request.getEmail());
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setDateCreationCompte(new Date());
        utilisateur.setRole(Role.CLIENT);

        // Oracle DB still has NOT NULL on username column — auto-generate from email prefix
        String baseUsername = request.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "");
        String username = baseUsername;
        int suffix = 1;
        while (clientRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }
        utilisateur.setUsername(username);

        // KYC fields (cin, dateNaissance, job, adresse, etc.) collected later at bank account opening

        Utilisateur saved = clientRepository.save(utilisateur);

        String token = jwtService.generateToken(saved.getIdUtilisateur(), saved.getRole().toString());
        return new AuthResponse(token);
    }


    @Override
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token, jwtService.extractExpiration(token));
        }
    }
}