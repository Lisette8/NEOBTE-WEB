package com.sesame.neobte.DTO.Responses.Virement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VirementHistoryPageDTO {
    private List<VirementResponseDTO> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    /** Sum of (montant + frais) on outgoing virements in this result set */
    private double totalSent;

    /** Sum of montant on incoming virements in this result set */
    private double totalReceived;
}
