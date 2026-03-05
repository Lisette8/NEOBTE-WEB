package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Entities.Compte;
import com.sesame.neobte.Entities.Virement;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class VirementServiceImpl implements VirementService {

    private IVirementRepository virementRepository;
    private ICompteRepository compteRepository;

    @Override
    public VirementResponseDTO effectuerVirement(VirementCreateDTO dto) {

        Compte compteSource = compteRepository.findById(dto.getCompteSourceId())
                .orElseThrow(() -> new RuntimeException("Compte source introuvable"));

        Compte compteDestination = compteRepository.findById(dto.getCompteDestinationId())
                .orElseThrow(() -> new RuntimeException("Compte destination introuvable"));

        if (compteSource.getSolde() < dto.getMontant()) {
            throw new RuntimeException("Solde insuffisant");
        }

        // update balances section
        compteSource.setSolde(compteSource.getSolde() - dto.getMontant());
        compteDestination.setSolde(compteDestination.getSolde() + dto.getMontant());

        compteRepository.save(compteSource);
        compteRepository.save(compteDestination);

        Virement virement = new Virement();
        virement.setCompteDe(compteSource);
        virement.setCompteA(compteDestination);
        virement.setMontant(dto.getMontant());
        virement.setDateDeVirement(new Date());

        Virement saved = virementRepository.save(virement);

        return mapToResponseDTO(saved);

    }


    @Override
    public List<VirementResponseDTO> getVirementsCompte(Long compteId) {

        List<Virement> virements = virementRepository.findByCompteDeIdCompte(compteId);

        List<VirementResponseDTO> response = new ArrayList<>();

        for (Virement v : virements) {
            response.add(mapToResponseDTO(v));
        }

        return response;
    }


    //private functions
    private VirementResponseDTO mapToResponseDTO(Virement virement) {

        return new VirementResponseDTO(
                virement.getIdVirement(),
                virement.getCompteDe().getIdCompte(),
                virement.getCompteA().getIdCompte(),
                virement.getMontant(),
                virement.getDateDeVirement()
        );
    }
}

