package com.sesame.neobte.Mappers_Example;

import com.sesame.neobte.DTO.Responses.Admin.AdminUserResponse;
import com.sesame.neobte.Entities.Class.Utilisateur;


//I created this mapper just to practice and have a clear look , the other mappers will be created and implemented directly in the services...
public class UtilisateurMapper {

    //adminResponse
    public static AdminUserResponse toAdminResponse(Utilisateur user) {

        double totalSolde = user.getComptes() == null ? 0.0 :
                user.getComptes()
                        .stream()
                        .mapToDouble(compte -> compte.getSolde() != null ? compte.getSolde() : 0.0)
                        .sum();


        return new AdminUserResponse(
                user.getIdUtilisateur(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getAge(),
                user.getAdresse(),
                user.getJob(),
                user.getGenre() != null ? user.getGenre().name() : null,
                totalSolde,
                user.getRole()
        );
    }


}
