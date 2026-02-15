package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.AdminRequests.CreateUserRequest;
import com.sesame.neobte.DTO.AdminRequests.UpdateUserRequest;
import com.sesame.neobte.Entities.Utilisateur;

import java.util.List;

public interface AdministrateurService {
    List<Utilisateur> getAllUsers();
    Utilisateur getUserById(Long id);
    Utilisateur createUtilisateur(CreateUserRequest dto);
    Utilisateur updateUser(Long id, UpdateUserRequest dto);
    void deleteUser(Long id);
}
