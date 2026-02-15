package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.AuthResponse;
import com.sesame.neobte.DTO.LoginRequest;
import com.sesame.neobte.DTO.RegisterRequest;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Entities.Role;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Security.JwtService;
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
                utilisateur.getIdClient(),
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


    public void logout() {
        // For now, nothing to do
        // Later with JWT: you can blacklist token or handle session
    }
}
