package com.sesame.neobte.Entities.Class;

import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReferralReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The existing client who shared their code. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Utilisateur referrer;

    /** The newly registered client who used the code. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Utilisateur referred;

    /** The code that was used (snapshot, for audit). */
    @Column(nullable = false)
    private String codeUsed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateReferral;

    /** Whether the Premium reward has been applied to the referrer. */
    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean rewarded = false;

    @PrePersist
    protected void onCreate() {
        this.dateReferral = LocalDateTime.now();
    }
}