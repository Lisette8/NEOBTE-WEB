package com.sesame.neobte.DTO.Responses.Client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponse {
    private Long id;
    private String email;
    private String nom;
    private String prenom;

}
