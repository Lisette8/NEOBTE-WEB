package com.sesame.neobte.DTO.Responses.Search;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GlobalSearchResponseDTO {

    private List<UserResult> users;
    private List<AccountResult>   accounts;
    private List<TransferResult>  transfers;
    private List<SupportResult>   tickets;

    @Getter @Builder @AllArgsConstructor
    public static class UserResult {
        private Long   id;
        private String fullName;
        private String email;
        private String role;
        private boolean premium;
    }

    @Getter @Builder @AllArgsConstructor
    public static class AccountResult {
        private Long   id;
        private String type;
        private String statut;
        private Double solde;
        private Long   userId;
        private String userFullName;
    }

    @Getter @Builder @AllArgsConstructor
    public static class TransferResult {
        private Long   id;
        private Double montant;
        private String senderName;
        private String recipientName;
        private Date date;
    }

    @Getter @Builder @AllArgsConstructor
    public static class SupportResult {
        private Long   id;
        private String sujet;
        private String status;
        private String userEmail;
    }
}
