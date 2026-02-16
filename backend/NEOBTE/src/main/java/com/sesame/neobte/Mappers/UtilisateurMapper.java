package com.sesame.neobte.Mappers;

import com.sesame.neobte.DTO.Responses.Admin.AdminUserResponse;
import com.sesame.neobte.DTO.Responses.Client.ClientResponse;
import com.sesame.neobte.Entities.Utilisateur;
import lombok.AllArgsConstructor;



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


    //clientResponse
    public static ClientResponse toClientResponse(Utilisateur user) {
        ClientResponse res = new ClientResponse();
        res.setId(user.getIdUtilisateur());
        res.setEmail(user.getEmail());
        res.setNom(user.getNom());
        res.setPrenom(user.getPrenom());
        return res;
    }
}
