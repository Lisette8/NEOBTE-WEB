package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Class.Virement;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class VirementServiceImpl implements VirementService {

    private IVirementRepository virementRepository;
    private ICompteRepository compteRepository;



    //client
    @Transactional //if something goes wrong, everything will be undone
    @Override
    public VirementResponseDTO effectuerVirement(VirementCreateDTO dto) {

        //check if key is present
        if(dto.getIdempotencyKey() == null){
            throw new RuntimeException("Idempotency key is required");
        }

        //check duplicate request
        Optional<Virement> existing =
                virementRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if(existing.isPresent()){
            return mapToResponseDTO(existing.get());
        }


        //validation
        if(dto.getMontant() <= 0){
            throw new RuntimeException("Montant doit etre superieur a 0");
        }

        if(dto.getCompteSourceId().equals(dto.getCompteDestinationId())){
            throw new RuntimeException("Vous ne pouvez pas transferer de l'argent vers le meme compte");
        }

        //one of the best practices when it comes to PESSIMISTIC_WRITE is to always lock the source account and then the destination account
        Compte compteSource = compteRepository.findByIdForUpdate(dto.getCompteSourceId())
                .orElseThrow(() -> new RuntimeException("Compte source introuvable"));

        Compte compteDestination = compteRepository.findByIdForUpdate(dto.getCompteDestinationId())
                .orElseThrow(() -> new RuntimeException("Compte destination introuvable"));

        if(compteSource.getStatutCompte() != StatutCompte.ACTIF){
            throw new RuntimeException("Compte source non actif");
        }

        if (compteSource.getSolde() < dto.getMontant()) {
            throw new RuntimeException("Solde insuffisant");
        }

        // update balances and transfer section
        compteSource.setSolde(compteSource.getSolde() - dto.getMontant());
        compteDestination.setSolde(compteDestination.getSolde() + dto.getMontant());

        compteRepository.save(compteSource);
        compteRepository.save(compteDestination);

        Virement virement = new Virement();
        virement.setCompteDe(compteSource);
        virement.setCompteA(compteDestination);
        virement.setMontant(dto.getMontant());
        virement.setDateDeVirement(new Date());
        virement.setIdempotencyKey(dto.getIdempotencyKey());

        Virement saved = virementRepository.save(virement);

        return mapToResponseDTO(saved);

    }


    @Override
    public List<VirementResponseDTO> getVirementsCompte(Long compteId) {

        List<Virement> virements = virementRepository.findByCompteDeIdCompteOrCompteAIdCompte(compteId ,compteId)
                .stream()
                .sorted(Comparator.comparing(Virement::getDateDeVirement).reversed())
                .toList();

        return virements.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }



    //admin
    @Override
    public List<VirementResponseDTO> getAllVirements() {
        List<Virement> virements = virementRepository.findAll();

        return virements.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }


    @Override
    public VirementResponseDTO getVirementById(Long virementId) {
        Virement virement = virementRepository.findById(virementId)
                .orElseThrow(() -> new RuntimeException("Virement introuvable"));

        return mapToResponseDTO(virement);
    }


    //private functions
    private VirementResponseDTO mapToResponseDTO(Virement virement) {

        return new VirementResponseDTO(
                virement.getIdVirement(),
                virement.getCompteDe().getIdCompte(),
                virement.getCompteA().getIdCompte(),
                virement.getMontant(),
                virement.getDateDeVirement(),
                virement.getIdempotencyKey()
        );
    }
}

