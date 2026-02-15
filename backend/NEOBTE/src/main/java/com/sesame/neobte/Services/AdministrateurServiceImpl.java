package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.AdminRequests.CreateUserRequest;
import com.sesame.neobte.DTO.AdminRequests.UpdateUserRequest;
import com.sesame.neobte.Entities.Role;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class AdministrateurServiceImpl implements AdministrateurService {

    private IUtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;



    @Override
    public List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }


    @Override
    public Utilisateur getUserById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }


    @Override
    public Utilisateur createUtilisateur(CreateUserRequest dto) {
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setAdresse(dto.getAdresse());
        utilisateur.setAge(dto.getAge());
        utilisateur.setJob(dto.getJob());
        utilisateur.setGenre(dto.getGenre());

        utilisateur.setSolde(dto.getSolde() != null ? dto.getSolde() : 0.0);

        // 🔐 VERY IMPORTANT
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        // 🎭 role
        utilisateur.setRole(dto.getRole() != null ? dto.getRole() : Role.CLIENT);

        // optional: creation date
        utilisateur.setDateCreationCompte(new Date());

        return utilisateurRepository.save(utilisateur);
    }


    @Override
    public Utilisateur updateUser(Long id, UpdateUserRequest dto) {
        Utilisateur oldUser = getUserById(id);

        if (dto.getNom() != null)
            oldUser.setNom(dto.getNom());

        if (dto.getPrenom() != null)
            oldUser.setPrenom(dto.getPrenom());

        if (dto.getAdresse() != null)
            oldUser.setAdresse(dto.getAdresse());

        if (dto.getAge() != null)
            oldUser.setAge(dto.getAge());

        if (dto.getJob() != null)
            oldUser.setJob(dto.getJob());

        if (dto.getGenre() != null)
            oldUser.setGenre(dto.getGenre());

        if (dto.getSolde() != null)
            oldUser.setSolde(dto.getSolde());

        if (dto.getMotDePasse() != null)
            oldUser.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        if (dto.getRole() != null)
            oldUser.setRole(dto.getRole());


        return utilisateurRepository.save(oldUser);
    }


    @Override
    public void deleteUser(Long id) {
        utilisateurRepository.deleteById(id);
    }
}
