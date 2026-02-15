package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private IUtilisateurRepository clientRepository;
    private PasswordEncoder passwordEncoder;

    //methods
    @Override
    public Utilisateur createClient(Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return clientRepository.save(utilisateur);
    }

    @Override
    public List<Utilisateur> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public Utilisateur getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No client found with id " + id));
    }

    @Override
    public Utilisateur updateClient(Long id, Utilisateur newUtilisateur) {
        Utilisateur oldUtilisateur = getClientById(id);

        oldUtilisateur.setNom(newUtilisateur.getNom());
        oldUtilisateur.setPrenom(newUtilisateur.getPrenom());
        oldUtilisateur.setAge(newUtilisateur.getAge());
        oldUtilisateur.setAdresse(newUtilisateur.getAdresse());
        oldUtilisateur.setJob(newUtilisateur.getJob());
        oldUtilisateur.setGenre(newUtilisateur.getGenre());

        return clientRepository.save(oldUtilisateur);
    }

    @Override
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }
}
