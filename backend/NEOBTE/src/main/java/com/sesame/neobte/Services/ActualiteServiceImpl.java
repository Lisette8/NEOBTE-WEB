package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Entities.Class.Actualite;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Repositories.IActualiteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class ActualiteServiceImpl implements ActualiteService {

    private IActualiteRepository actualiteRepository;
    private IUtilisateurRepository utilisateurRepository;

    
    //admin crud
    @Override
    public Page<ActualiteResponseDTO> getAll(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Actualite> actualites =
                actualiteRepository.findAllByOrderByDateCreationActualiteDesc(pageable);

        return actualites.map(this::mapToResponseDTO);
    }


    @Override
    public ActualiteResponseDTO getById(Long id) {
        Actualite actualite = actualiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actualite not found"));

        return mapToResponseDTO(actualite);
    }


    @Override
    public ActualiteResponseDTO createActualite(Long adminId, String titre, String description) {

        Utilisateur admin = utilisateurRepository.  findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Actualite actualite = new Actualite();
        actualite.setTitre(titre);
        actualite.setDescription(description);
        actualite.setDateCreationActualite(LocalDateTime.now());
        actualite.setCreateur(admin);

        Actualite savedActualite = actualiteRepository.save(actualite);

        return mapToResponseDTO(savedActualite);
    }



    @Override
    public ActualiteResponseDTO  updateActualite(Long id, String titre, String description) {

        Actualite actualiteUpdated = actualiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actualite not found"));

        actualiteUpdated.setTitre(titre);
        actualiteUpdated.setDescription(description);

        Actualite savedActualite = actualiteRepository.save(actualiteUpdated);

        return mapToResponseDTO(savedActualite);
    }


    @Override
    public void deleteActualite(Long id) {
        actualiteRepository.deleteById(id);
    }



    //Private functions
    private ActualiteResponseDTO mapToResponseDTO(Actualite actualite) {

        return new ActualiteResponseDTO(
                actualite.getIdActualite(),
                actualite.getTitre(),
                actualite.getDescription(),
                actualite.getDateCreationActualite(),
                actualite.getCreateur().getIdUtilisateur()
        );
    }

}
