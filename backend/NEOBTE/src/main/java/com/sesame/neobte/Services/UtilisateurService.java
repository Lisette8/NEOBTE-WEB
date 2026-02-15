package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Utilisateur;

import java.util.*;

public interface UtilisateurService {

    Utilisateur createClient(Utilisateur utilisateur);
    List<Utilisateur> getAllClients();
    Utilisateur getClientById(Long id);
    Utilisateur updateClient(Long id, Utilisateur newUtilisateur);
    void deleteClient(Long id);
}
