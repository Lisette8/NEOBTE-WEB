package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.UtilisateurRequests.UpdateProfileRequest;
import com.sesame.neobte.Entities.Utilisateur;

import java.util.*;

public interface UtilisateurService {

    Utilisateur createUtilisateur(Utilisateur utilisateur);
    Utilisateur getUtilisateurById(Long id);
    Utilisateur updateUtilisateur(Long id, UpdateProfileRequest dto);
    void deleteUtilisateur(Long id);
}
