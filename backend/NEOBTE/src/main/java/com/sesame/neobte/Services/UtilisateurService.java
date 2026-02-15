package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Utilisateur;

import java.util.*;

public interface UtilisateurService {

    Utilisateur createUtilisateur(Utilisateur utilisateur);
    List<Utilisateur> getAllUtilisateur();
    Utilisateur getUtilisateurById(Long id);
    Utilisateur updateUtilisateur(Long id, Utilisateur newUtilisateur);
    void deleteUtilisateur(Long id);
}
