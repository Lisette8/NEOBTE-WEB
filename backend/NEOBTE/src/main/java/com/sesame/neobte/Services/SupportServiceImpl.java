package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Support;
import com.sesame.neobte.Entities.SupportStatus;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Repositories.ISupportRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class SupportServiceImpl implements SupportService {

    private ISupportRepository supportRepository;
    private IUtilisateurRepository utilisateurRepository;



    @Override
    public List<Support> getMyTickets(Long userId) {
        return supportRepository.findByUtilisateurIdUtilisateur(userId);
    }


    @Override
    public List<Support> getAllTickets() {
        return supportRepository.findAll();
    }


    //crud support

    @Override
    public Support createTicket(Long userId, String sujet, String message) {

        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Support support = new Support();
        support.setSujet(sujet);
        support.setMessage(message);
        support.setStatus(SupportStatus.OPEN);
        support.setDateCreation(new Date());
        support.setUtilisateur(user);

        return supportRepository.save(support);
    }


    @Override
    public Support updateStatus(Long id, String response, String status) {

        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        support.setReponseAdmin(response);
        support.setStatus(SupportStatus.valueOf(status));

        return supportRepository.save(support);
    }


    @Override
    public void deleteTicket(Long id) {
        supportRepository.deleteById(id);
    }
}
