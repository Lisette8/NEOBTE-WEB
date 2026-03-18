package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Admin.CreateUserRequest;
import com.sesame.neobte.DTO.Requests.Admin.UpdateUserRequest;
import com.sesame.neobte.DTO.Responses.Admin.AdminUserResponse;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class AdministrateurServiceImpl implements AdministrateurService {

    private final IUtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public List<AdminUserResponse> getAllUsers() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }


    public Utilisateur getUserEntityById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }


    @Override
    public AdminUserResponse getUserById(Long id) {
        Utilisateur user = getUserEntityById(id);
        return toAdminResponse(user);
    }


    @Override
    public Utilisateur createUtilisateur(CreateUserRequest dto) {
        Utilisateur utilisateur = new Utilisateur();

        if(utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if(utilisateurRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if(utilisateurRepository.existsByCin(dto.getCin())) {
            throw new BadRequestException("CIN already exists");
        }

        utilisateur.setEmail(dto.getEmail());
        utilisateur.setUsername(dto.getUsername());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setCin(dto.getCin());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setAdresse(dto.getAdresse());
        utilisateur.setCodePostal(dto.getCodePostal());
        utilisateur.setPays(dto.getPays() != null ? dto.getPays() : "Tunisie");
        utilisateur.setDateNaissance(dto.getDateNaissance());
        utilisateur.setJob(dto.getJob());
        utilisateur.setGenre(dto.getGenre());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        utilisateur.setRole(dto.getRole() != null ? dto.getRole() : Role.CLIENT);
        utilisateur.setDateCreationCompte(new Date());

        return utilisateurRepository.save(utilisateur);
    }


    @Override
    public Utilisateur updateUser(Long id, UpdateUserRequest dto) {
        Utilisateur user = getUserEntityById(id);

        if (dto.getNom() != null)          user.setNom(dto.getNom());
        if (dto.getPrenom() != null)        user.setPrenom(dto.getPrenom());
        if (dto.getTelephone() != null)     user.setTelephone(dto.getTelephone());
        if (dto.getAdresse() != null)       user.setAdresse(dto.getAdresse());
        if (dto.getCodePostal() != null)    user.setCodePostal(dto.getCodePostal());
        if (dto.getPays() != null)          user.setPays(dto.getPays());
        if (dto.getDateNaissance() != null) user.setDateNaissance(dto.getDateNaissance());
        if (dto.getJob() != null)           user.setJob(dto.getJob());
        if (dto.getGenre() != null)         user.setGenre(dto.getGenre());
        if (dto.getMotDePasse() != null)    user.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        if (dto.getRole() != null)          user.setRole(dto.getRole());

        return utilisateurRepository.save(user);
    }


    @Override
    public void deleteUser(Long id) {
        Utilisateur user = getUserEntityById(id);
        utilisateurRepository.delete(user);
    }

    @Override
    public void setPremium(Long id, boolean premium) {
        Utilisateur user = getUserEntityById(id);
        user.setPremium(premium);
        utilisateurRepository.save(user);
    }


    // mapper
    private AdminUserResponse toAdminResponse(Utilisateur user) {
        double totalSolde = user.getComptes() == null ? 0.0 :
                user.getComptes().stream()
                        .mapToDouble(c -> c.getSolde() != null ? c.getSolde() : 0.0)
                        .sum();

        return AdminUserResponse.builder()
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
                .totalSolde(totalSolde)
                .premium(user.isPremium())
                .build();
    }
}
