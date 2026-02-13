package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.LoginRequest;
import com.sesame.neobte.DTO.RegisterRequest;
import com.sesame.neobte.Entities.Client;
import com.sesame.neobte.Repositories.IClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private IClientRepository clientRepository;
    private PasswordEncoder passwordEncoder;


    @Override
    public Client login(LoginRequest request) {
        Client client = clientRepository.findByEmail(request.getEmail());

        if (client == null) {
            throw new RuntimeException("No client with email " + request.getEmail());
        }

        boolean ifTrueMDP = passwordEncoder.matches(
                request.getMotDePasse(),
                client.getMotDePasse()
        );

        if(!ifTrueMDP){
            throw new RuntimeException("Wrong password");
        }

        System.out.println("User logged in");
        return client;

    }


    @Override
    public Client register(RegisterRequest request) {

        Client checkClientAlreadyExists = clientRepository.findByEmail(request.getEmail());
        if (checkClientAlreadyExists != null) {
            throw new RuntimeException("Client with email " + request.getEmail() + " already exists");
        }

        Client client = new Client();

        client.setEmail(request.getEmail());
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setAdresse(request.getAdresse());
        client.setAge(request.getAge());
        client.setJob(request.getJob());
        client.setGenre(request.getGenre());
        client.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        client.setDateCreationCompte(new Date());
        client.setSolde(0.0);

        System.out.println("User registered");
        return clientRepository.save(client);
    }


    public void logout() {
        // For now, nothing to do
        // Later with JWT: you can blacklist token or handle session
    }
}
