package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Client.ChangePasswordRequest;
import com.sesame.neobte.DTO.Requests.Client.UpdateProfileRequest;
import com.sesame.neobte.Entities.Utilisateur;

public interface UtilisateurService {

    Utilisateur createUtilisateur(Utilisateur utilisateur);
    Utilisateur getUtilisateurById(Long id);
    Utilisateur updateUtilisateur(Long id, UpdateProfileRequest dto);
    void changePassword(Long id, ChangePasswordRequest request);
    void deleteUtilisateur(Long id);
}
