package com.sesame.neobte.Controllers.Virement;

import com.sesame.neobte.DTO.Responses.Virement.TreasuryResponseDTO;
import com.sesame.neobte.Entities.Class.FraisTransaction;
import com.sesame.neobte.Repositories.ICompteInterneRepository;
import com.sesame.neobte.Repositories.IFraisTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/treasury")
public class TreasuryController {

    private final ICompteInterneRepository compteInterneRepository;
    private final IFraisTransactionRepository fraisTransactionRepository;

    @Value("${neobte.transfer.fee-rate:0.005}")
    private double feeRate;

    @Value("${neobte.fee-account.name:NEOBTE_FEES}")
    private String feeAccountName;

    public TreasuryController(
            ICompteInterneRepository compteInterneRepository,
            IFraisTransactionRepository fraisTransactionRepository) {
        this.compteInterneRepository = compteInterneRepository;
        this.fraisTransactionRepository = fraisTransactionRepository;
    }

    @GetMapping
    public TreasuryResponseDTO getTreasury() {

        Double total = compteInterneRepository.findByNom(feeAccountName)
                .map(c -> c.getSolde())
                .orElse(0.0);

        List<FraisTransaction> frais = fraisTransactionRepository.findAllByOrderByDateCreationDesc();

        List<TreasuryResponseDTO.FraisEntryDTO> entries = frais.stream()
                .limit(100)
                .map(f -> {
                    var v = f.getVirement();
                    String sender    = v.getCompteDe().getUtilisateur() != null
                            ? v.getCompteDe().getUtilisateur().getPrenom() + " " + v.getCompteDe().getUtilisateur().getNom()
                            : "—";
                    String recipient = v.getCompteA().getUtilisateur() != null
                            ? v.getCompteA().getUtilisateur().getPrenom() + " " + v.getCompteA().getUtilisateur().getNom()
                            : "—";
                    return new TreasuryResponseDTO.FraisEntryDTO(
                            f.getId(),
                            v.getIdVirement(),
                            f.getMontantFrais(),
                            f.getTauxApplique(),
                            v.getMontant(),
                            sender,
                            recipient,
                            f.getDateCreation()
                    );
                }).toList();

        return new TreasuryResponseDTO(total, feeRate, entries);
    }
}