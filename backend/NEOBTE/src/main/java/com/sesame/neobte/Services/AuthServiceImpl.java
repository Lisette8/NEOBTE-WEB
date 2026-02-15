package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.AuthResponse;
import com.sesame.neobte.DTO.LoginRequest;
import com.sesame.neobte.DTO.RegisterRequest;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Entities.Role;
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
            throw new RuntimeException("No client with email " + request.getEmail());
        }

        boolean ifTrueMDP = passwordEncoder.matches(
                request.getMotDePasse(),
                utilisateur.getMotDePasse()
        );

        if(!ifTrueMDP){
            throw new RuntimeException("Wrong password");
        }

        String token = jwtService.generateToken(
                utilisateur.getIdUtilisateur(),
                utilisateur.getRole().toString()
                );


        System.out.println("User logged in");
        return new AuthResponse(token);

    }


    @Override
    public Utilisateur register(RegisterRequest request) {

        Utilisateur checkUtilisateurAlreadyExists = clientRepository.findByEmail(request.getEmail());
        if (checkUtilisateurAlreadyExists != null) {
            throw new RuntimeException("Client with email " + request.getEmail() + " already exists");
        }

        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setEmail(request.getEmail());
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setAdresse(request.getAdresse());
        utilisateur.setAge(request.getAge());
        utilisateur.setJob(request.getJob());
        utilisateur.setGenre(request.getGenre());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setDateCreationCompte(new Date());
        utilisateur.setSolde(0.0);
        utilisateur.setRole(Role.CLIENT);

        System.out.println("User registered");
        return clientRepository.save(utilisateur);
    }


    @Override
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
    }
}
