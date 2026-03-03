package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Actualite;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Repositories.IActualiteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ActualiteServiceImpl implements ActualiteService {

    private IActualiteRepository actualiteRepository;
    private IUtilisateurRepository utilisateurRepository;


    //admin crud
    @Override
    public List<Actualite> getAll() {
        return actualiteRepository.findAll();
    }


    @Override
    public Actualite createActualite(Long adminId, String titre, String description) {

        Utilisateur admin = utilisateurRepository.  findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Actualite actualite = new Actualite();
        actualite.setTitre(titre);
        actualite.setDescription(description);
        actualite.setDateCreationActualite(new Date());
        actualite.setCreateur(admin);

        return actualiteRepository.save(actualite);
    }


    @Override
    public Actualite updateActualite(Long id, String titre, String description) {

        Actualite actualiteUpdated = actualiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actualite not found"));

        actualiteUpdated.setTitre(titre);
        actualiteUpdated.setDescription(description);

        return actualiteRepository.save(actualiteUpdated);
    }


    @Override
    public void deleteActualite(Long id) {
        actualiteRepository.deleteById(id);
    }
}
