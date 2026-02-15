package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.UtilisateurRequests.UpdateProfileRequest;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private IUtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;

    //methods
    @Override
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }


    @Override
    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No client found with id " + id));
    }


    @Override
    public Utilisateur updateUtilisateur(Long id, UpdateProfileRequest dto) {
        Utilisateur oldUtilisateur = getUtilisateurById(id);

        oldUtilisateur.setNom(dto.getNom());
        oldUtilisateur.setPrenom(dto.getPrenom());
        oldUtilisateur.setAge(dto.getAge());
        oldUtilisateur.setAdresse(dto.getAdresse());
        oldUtilisateur.setJob(dto.getJob());
        oldUtilisateur.setGenre(dto.getGenre());

        System.out.println("Client updated");
        return utilisateurRepository.save(oldUtilisateur);
    }


    @Override
    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }
}
