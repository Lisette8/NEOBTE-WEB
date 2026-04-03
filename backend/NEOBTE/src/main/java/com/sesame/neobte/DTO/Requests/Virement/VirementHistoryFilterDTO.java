package com.sesame.neobte.DTO.Requests.Virement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VirementHistoryFilterDTO {
    private String search;
    private String period = "all";
    private String type = "all";
    private String sort = "date-desc";
    private int page = 0;
    private int size = 20;
}
