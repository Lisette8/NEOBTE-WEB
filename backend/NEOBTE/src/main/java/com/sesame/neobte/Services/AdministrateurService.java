package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Admin.CreateUserRequest;
import com.sesame.neobte.DTO.Requests.Admin.UpdateUserRequest;
import com.sesame.neobte.DTO.Responses.Admin.AdminUserResponse;
import com.sesame.neobte.Entities.Class.Utilisateur;

import java.util.List;

public interface AdministrateurService {
    List<AdminUserResponse> getAllUsers();
    Utilisateur getUserEntityById(Long id);
    AdminUserResponse getUserById(Long id);
    Utilisateur createUtilisateur(CreateUserRequest dto);
    Utilisateur updateUser(Long id, UpdateUserRequest dto);
    void deleteUser(Long id);
}
