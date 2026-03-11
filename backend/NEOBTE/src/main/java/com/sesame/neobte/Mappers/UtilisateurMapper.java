package com.sesame.neobte.Mappers;

import com.sesame.neobte.DTO.Responses.Admin.AdminUserResponse;
import com.sesame.neobte.Entities.Class.Utilisateur;


//I created this mapper just to practice and have a clear look , the other mappers will be created and implemented directly in the services...
public class UtilisateurMapper {

    //adminResponse
    public static AdminUserResponse toAdminResponse(Utilisateur user) {
        return new AdminUserResponse(
                user.getIdUtilisateur(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getAge(),
                user.getAdresse(),
                user.getJob(),
                user.getGenre() != null ? user.getGenre().name() : null,
                user.getSolde(),
                user.getRole()
        );
    }


}
