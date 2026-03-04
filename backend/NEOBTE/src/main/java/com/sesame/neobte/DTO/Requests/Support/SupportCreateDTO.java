package com.sesame.neobte.DTO.Requests.Support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SupportCreateDTO {
    private String sujet;
    private String message;

}
