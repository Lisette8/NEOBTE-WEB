package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Client.ChangePasswordRequest;
import com.sesame.neobte.DTO.Requests.Client.UpdateProfileRequest;
import com.sesame.neobte.DTO.Responses.Client.ClientResponse;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final IUtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }


    @Override
    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }


    @Override
    public Utilisateur updateUtilisateur(Long id, UpdateProfileRequest dto) {
        Utilisateur user = getUtilisateurById(id);

        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setTelephone(dto.getTelephone());
        user.setJob(dto.getJob());
        user.setGenre(dto.getGenre());
        user.setAdresse(dto.getAdresse());
        user.setCodePostal(dto.getCodePostal());
        user.setPays(dto.getPays());

        return utilisateurRepository.save(user);
    }


    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        Utilisateur user = getUtilisateurById(id);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getMotDePasse())) {
            throw new BadRequestException("Old password is incorrect");
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(user);
    }


    @Override
    public void deleteUtilisateur(Long id) {
        Utilisateur user = getUtilisateurById(id);
        utilisateurRepository.delete(user);
    }


    // mapper
    public ClientResponse mapToClientResponse(Utilisateur user) {
        return ClientResponse.builder()
                .id(user.getIdUtilisateur())
                .email(user.getEmail())
                .username(user.getUsername())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .cin(user.getCin())
                .telephone(user.getTelephone())
                .adresse(user.getAdresse())
                .codePostal(user.getCodePostal())
                .pays(user.getPays())
                .dateNaissance(user.getDateNaissance())
                .job(user.getJob())
                .genre(user.getGenre())
                .role(user.getRole())
                .dateCreationCompte(user.getDateCreationCompte())
                .build();
    }
}