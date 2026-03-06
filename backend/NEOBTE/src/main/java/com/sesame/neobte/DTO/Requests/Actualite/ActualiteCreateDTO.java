package com.sesame.neobte.DTO.Requests.Actualite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ActualiteCreateDTO {

    private String titre;
    private String description;
}
