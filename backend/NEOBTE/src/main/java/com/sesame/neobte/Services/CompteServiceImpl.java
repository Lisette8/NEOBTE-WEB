package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Compte.CompteCreateDTO;
import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CompteServiceImpl implements CompteService {

    private final ICompteRepository compteRepository;
    private final IUtilisateurRepository utilisateurRepository;


    @Override
    public CompteResponseDTO createCompte(CompteCreateDTO dto) {

        Utilisateur user = utilisateurRepository.findById(dto.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Compte compte = new Compte();

        compte.setSolde(dto.getSolde());
        compte.setTypeCompte(dto.getTypeCompte());
        compte.setStatutCompte(StatutCompte.ACTIVE);
        compte.setUtilisateur(user);

        Compte saved = compteRepository.save(compte);

        return mapToCompteDTO(saved);
    }


    @Override
    public List<CompteResponseDTO> getAllComptes() {
        return compteRepository.findAll()
                .stream()
                .map(this::mapToCompteDTO)
                .toList();
    }


    @Override
    public CompteResponseDTO getCompteById(Long id) {
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found"));

        return mapToCompteDTO(compte);
    }


    @Override
    public void deleteCompteById(Long id) {
        compteRepository.deleteById(id);
    }


    @Override
    public List<CompteResponseDTO> getComptesByUtilisateur(Long userId) {
        return compteRepository.findByUtilisateur_IdUtilisateur(userId)
                .stream()
                .map(this::mapToCompteDTO)
                .toList();
    }


    //Private functions
    private CompteResponseDTO mapToCompteDTO(Compte compte) {
        CompteResponseDTO dto = new CompteResponseDTO();

        dto.setIdCompte(compte.getIdCompte());
        dto.setSolde(compte.getSolde());
        dto.setTypeCompte(compte.getTypeCompte());
        dto.setStatutCompte(compte.getStatutCompte());

        if (compte.getUtilisateur() != null) {
            dto.setUtilisateurId(compte.getUtilisateur().getIdUtilisateur());
        }
        return dto;
    }
}