package com.sesame.neobte.DTO.Responses.Referral;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReferralDashboardDTO {
    private String referralCode;
    private String referralLink;
    private long totalReferrals;
    private boolean premium;
    private LocalDateTime premiumExpiresAt;
    private List<ReferralEntryDTO> referrals;

    @Getter
    @Builder
    public static class ReferralEntryDTO {
        private Long id;
        private String referredName;
        private String referredEmail;
        private LocalDateTime dateReferral;
        private boolean rewarded;
    }
}
