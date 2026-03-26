package com.sesame.neobte.Services.Investment;

import com.sesame.neobte.DTO.Requests.Investment.InvestmentCreateDTO;
import com.sesame.neobte.DTO.Requests.Investment.InvestmentPlanCreateDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentPlanResponseDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentResponseDTO;
import com.sesame.neobte.DTO.Responses.Investment.MonthlyEarningDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.CompteInterne;
import com.sesame.neobte.Entities.Class.Investment.Investment;
import com.sesame.neobte.Entities.Class.Investment.InvestmentPlan;
import com.sesame.neobte.Entities.Class.Investment.MonthlyEarning;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Investment.InvestmentStatut;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.ICompteInterneRepository;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.Investment.IInvestmentPlanRepository;
import com.sesame.neobte.Repositories.Investment.IInvestmentRepository;
import com.sesame.neobte.Repositories.Investment.IMonthlyEarningRepository;
import com.sesame.neobte.Services.NotificationService;
import com.sesame.neobte.Services.Other.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentServiceImpl implements InvestmentService {

    // Internal account names — constants so loans can reference them too
    public static final String ACCOUNT_INVESTMENTS = "NEOBTE_INVESTMENTS";
    public static final String ACCOUNT_RESERVES    = "NEOBTE_RESERVES";
    public static final String ACCOUNT_DEPLOYED    = "NEOBTE_DEPLOYED";
    public static final String ACCOUNT_FEES        = "NEOBTE_FEES";

    private final IInvestmentRepository investmentRepository;
    private final IInvestmentPlanRepository planRepository;
    private final IMonthlyEarningRepository monthlyEarningRepository;
    private final ICompteRepository compteRepository;
    private final ICompteInterneRepository compteInterneRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${neobte.investment.reserve-rate:0.15}")
    private double reserveRate;

    // ── Plans (admin) ──────────────────────────────────────────────────────

    @Override
    public List<InvestmentPlanResponseDTO> getAllPlans() {
        return planRepository.findAll().stream().map(this::mapPlan).toList();
    }

    @Override
    public List<InvestmentPlanResponseDTO> getActivePlans() {
        return planRepository.findByActifTrueOrderByDureeEnMoisAsc().stream().map(this::mapPlan).toList();
    }

    @Override @Transactional
    public InvestmentPlanResponseDTO createPlan(InvestmentPlanCreateDTO dto) {
        InvestmentPlan plan = new InvestmentPlan();
        applyPlanDto(plan, dto);
        return mapPlan(planRepository.save(plan));
    }

    @Override @Transactional
    public InvestmentPlanResponseDTO updatePlan(Long id, InvestmentPlanCreateDTO dto) {
        InvestmentPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan introuvable"));
        applyPlanDto(plan, dto);
        return mapPlan(planRepository.save(plan));
    }

    @Override @Transactional
    public void deletePlan(Long id) {
        InvestmentPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan introuvable"));
        boolean hasActive = investmentRepository.findByStatutOrderByDateDebutDesc(InvestmentStatut.ACTIVE)
                .stream().anyMatch(inv -> inv.getPlan().getId().equals(id));
        if (hasActive) { plan.setActif(false); planRepository.save(plan); }
        else planRepository.delete(plan);
    }

    // ── Client subscribe ───────────────────────────────────────────────────

    @Override @Transactional
    public InvestmentResponseDTO subscribe(InvestmentCreateDTO dto, Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        InvestmentPlan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan d'investissement introuvable"));

        if (!plan.isActif())
            throw new BadRequestException("Ce plan n'est plus disponible à la souscription.");
        if (dto.getMontant() < plan.getMontantMin())
            throw new BadRequestException(String.format("Montant minimum : %.3f TND.", plan.getMontantMin()));
        if (dto.getMontant() > plan.getMontantMax())
            throw new BadRequestException(String.format("Montant maximum : %.3f TND.", plan.getMontantMax()));

        // ── Debit client account ──────────────────────────────────────────
        Compte compte = compteRepository.findByIdForUpdate(dto.getCompteId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
        if (!compte.getUtilisateur().getIdUtilisateur().equals(userId))
            throw new BadRequestException("Ce compte ne vous appartient pas.");
        if (compte.getStatutCompte() != StatutCompte.ACTIVE)
            throw new BadRequestException("Votre compte doit être actif pour investir.");
        if (compte.getSolde() < dto.getMontant())
            throw new BadRequestException(String.format(
                    "Solde insuffisant. Disponible : %.3f TND. Requis : %.3f TND.",
                    compte.getSolde(), dto.getMontant()));

        compte.setSolde(compte.getSolde() - dto.getMontant());
        compteRepository.save(compte);

        // ── Route principal into the bank's internal accounts ─────────────
        double reserves = Math.round(dto.getMontant() * reserveRate * 1000.0) / 1000.0;
        double deployed = dto.getMontant() - reserves;

        credit(ACCOUNT_INVESTMENTS, dto.getMontant()); // full principal tracked as liability
        credit(ACCOUNT_RESERVES,    reserves);          // 15% liquid buffer
        credit(ACCOUNT_DEPLOYED,    deployed);          // 85% available to lend

        // ── Calculate interest schedule ───────────────────────────────────
        double monthlyInterest = Math.round(
                dto.getMontant() * plan.getTauxAnnuel() / 12.0 * 1000.0) / 1000.0;
        double interetAttendu  = Math.round(
                monthlyInterest * plan.getDureeEnMois() * 1000.0) / 1000.0;

        LocalDateTime now = LocalDateTime.now();
        Investment inv = new Investment();
        inv.setUtilisateur(user);
        inv.setCompte(compte);
        inv.setPlan(plan);
        inv.setPlanNom(plan.getNom());
        inv.setMontant(dto.getMontant());
        inv.setTauxAnnuel(plan.getTauxAnnuel());
        inv.setDureeEnMois(plan.getDureeEnMois());
        inv.setInteretAttendu(interetAttendu);
        inv.setDateDebut(now);
        inv.setDateEcheance(now.plusMonths(plan.getDureeEnMois()));
        inv.setStatut(InvestmentStatut.ACTIVE);
        Investment saved = investmentRepository.save(inv);

        // ── Pre-generate monthly earning schedule ─────────────────────────
        generateMonthlySchedule(saved, monthlyInterest, now);

        log.info("[INVEST] {} invested {} TND in '{}'. Pool↑ reserves={} deployed={}",
                userId, dto.getMontant(), plan.getNom(), reserves, deployed);

        notificationService.notifyUser(userId, NotificationType.INVESTMENT_CREATED,
                "Investissement activé",
                String.format("%.3f TND investis dans le plan « %s ». Échéance : %s. " +
                                "Intérêt mensuel attendu : %.3f TND.",
                        dto.getMontant(), plan.getNom(),
                        saved.getDateEcheance().toLocalDate().toString(), monthlyInterest),
                "/investment-view");

        return mapInvestment(saved);
    }

    // ── Client cancel (before maturity — principal returned, no interest) ──

    @Override @Transactional
    public InvestmentResponseDTO cancel(Long investmentId, Long userId) {
        Investment inv = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Investissement introuvable"));
        if (!inv.getUtilisateur().getIdUtilisateur().equals(userId))
            throw new BadRequestException("Cet investissement ne vous appartient pas.");
        if (inv.getStatut() != InvestmentStatut.ACTIVE)
            throw new BadRequestException("Seul un investissement actif peut être annulé.");

        // ── Return principal to client account ────────────────────────────
        Compte compte = compteRepository.findByIdForUpdate(inv.getCompte().getIdCompte()).orElseThrow();
        compte.setSolde(compte.getSolde() + inv.getMontant());
        compteRepository.save(compte);

        // ── Reverse the pool routing (unwind reserves / deployed) ─────────
        double reserves = Math.round(inv.getMontant() * reserveRate * 1000.0) / 1000.0;
        double deployed = inv.getMontant() - reserves;
        debit(ACCOUNT_INVESTMENTS, inv.getMontant());
        debit(ACCOUNT_RESERVES,    reserves);
        debit(ACCOUNT_DEPLOYED,    deployed);

        inv.setStatut(InvestmentStatut.CANCELLED);
        inv.setDateCloture(LocalDateTime.now());
        inv.setInteretVerse(0.0);
        investmentRepository.save(inv);

        // Mark all future monthly earnings as not accrued (they won't be paid)
        monthlyEarningRepository.findByInvestment_IdOrderByMoisNumeroAsc(investmentId)
                .stream().filter(e -> !e.isAccrued())
                .forEach(e -> { e.setMontantInteret(0.0); monthlyEarningRepository.save(e); });

        log.info("[INVEST] Cancelled {} — {} TND returned. Pool↓ reserves={} deployed={}",
                investmentId, inv.getMontant(), reserves, deployed);

        notificationService.notifyUser(userId, NotificationType.INVESTMENT_CREATED,
                "Investissement annulé",
                String.format("%.3f TND recrédités depuis « %s ». Aucun intérêt versé.",
                        inv.getMontant(), inv.getPlanNom()),
                "/investment-view");

        return mapInvestment(inv);
    }

    // ── Reads ──────────────────────────────────────────────────────────────

    @Override
    public List<InvestmentResponseDTO> getMyInvestments(Long userId) {
        return investmentRepository
                .findByUtilisateur_IdUtilisateurOrderByDateDebutDesc(userId)
                .stream().map(this::mapInvestment).toList();
    }

    @Override
    public List<InvestmentResponseDTO> getAllInvestments() {
        return investmentRepository.findAll().stream()
                .sorted((a, b) -> b.getDateDebut().compareTo(a.getDateDebut()))
                .map(this::mapInvestment).toList();
    }

    // ── Monthly accrual scheduler — runs on the 1st of every month at 00:05 ──

    @Scheduled(cron = "0 5 0 1 * *")
    @Transactional
    public void accrueMonthlyEarnings() {
        String currentMonth = YearMonth.now().toString(); // "2025-03"
        List<Investment> active = investmentRepository.findByStatutOrderByDateDebutDesc(InvestmentStatut.ACTIVE);
        log.info("[INVEST] Monthly accrual run for {} — {} active investments", currentMonth, active.size());

        for (Investment inv : active) {
            monthlyEarningRepository.findByInvestment_IdAndMois(inv.getId(), currentMonth)
                    .ifPresent(earning -> {
                        earning.setAccrued(true);
                        monthlyEarningRepository.save(earning);
                        log.info("[INVEST] Accrued {}.{} TND for investment {} ({})",
                                currentMonth, earning.getMontantInteret(), inv.getId(), inv.getPlanNom());
                    });
        }
    }

    // ── Maturity scheduler — every 60 seconds ─────────────────────────────

    @Override
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void matureAll() {
        List<Investment> due = investmentRepository.findMatured(LocalDateTime.now());
        if (due.isEmpty()) return;
        log.info("[INVEST] Maturing {} investment(s)", due.size());
        for (Investment inv : due) {
            try { mature(inv); }
            catch (Exception e) { log.error("[INVEST] Maturity failed for {}: {}", inv.getId(), e.getMessage()); }
        }
    }

    private void mature(Investment inv) {
        double principal = inv.getMontant();
        double interest  = inv.getInteretAttendu();
        double payout    = principal + interest;

        // ── Principal comes back from NEOBTE_INVESTMENTS ──────────────────
        double reserves = Math.round(principal * reserveRate * 1000.0) / 1000.0;
        double deployed = principal - reserves;
        debit(ACCOUNT_INVESTMENTS, principal);
        debit(ACCOUNT_RESERVES,    reserves);
        debit(ACCOUNT_DEPLOYED,    deployed);

        // ── Interest paid from NEOBTE_FEES (bank revenue funds the yield) ─
        debit(ACCOUNT_FEES, interest);

        // ── Credit full payout to client account ──────────────────────────
        Compte compte = compteRepository.findByIdForUpdate(inv.getCompte().getIdCompte()).orElseThrow();
        compte.setSolde(compte.getSolde() + payout);
        compteRepository.save(compte);

        // ── Mark all monthly earnings as accrued ──────────────────────────
        monthlyEarningRepository.findByInvestment_IdOrderByMoisNumeroAsc(inv.getId())
                .forEach(e -> { e.setAccrued(true); monthlyEarningRepository.save(e); });

        inv.setStatut(InvestmentStatut.COMPLETED);
        inv.setDateCloture(LocalDateTime.now());
        inv.setInteretVerse(interest);
        investmentRepository.save(inv);

        Long userId = inv.getUtilisateur().getIdUtilisateur();
        log.info("[INVEST] Matured {} — paid {} TND ({}p + {}i) to user {}",
                inv.getId(), payout, principal, interest, userId);

        notificationService.notifyUser(userId, NotificationType.INVESTMENT_MATURED,
                "Investissement arrivé à échéance !",
                String.format("« %s » a maturé. %.3f TND + %.3f TND intérêts = %.3f TND crédités.",
                        inv.getPlanNom(), principal, interest, payout),
                "/investment-view");

        try {
            emailService.sendInvestmentMaturedEmail(
                    inv.getUtilisateur().getEmail(), inv.getUtilisateur().getPrenom(),
                    inv.getPlanNom(), principal, interest, payout);
        } catch (Exception e) {
            log.warn("[INVEST] Maturity email failed for {}: {}", inv.getId(), e.getMessage());
        }
    }

    // ── Internal account helpers ───────────────────────────────────────────

    private void credit(String accountName, double amount) {
        CompteInterne account = compteInterneRepository.findByNomForUpdate(accountName)
                .orElseThrow(() -> new IllegalStateException("Compte interne introuvable: " + accountName));
        account.setSolde(account.getSolde() + amount);
        compteInterneRepository.save(account);
    }

    private void debit(String accountName, double amount) {
        CompteInterne account = compteInterneRepository.findByNomForUpdate(accountName)
                .orElseThrow(() -> new IllegalStateException("Compte interne introuvable: " + accountName));
        account.setSolde(Math.max(0.0, account.getSolde() - amount));
        compteInterneRepository.save(account);
    }

    // ── Monthly schedule generator ─────────────────────────────────────────

    private void generateMonthlySchedule(Investment inv, double monthlyInterest, LocalDateTime startDate) {
        YearMonth startMonth = YearMonth.from(startDate);
        List<MonthlyEarning> schedule = new ArrayList<>();
        for (int i = 0; i < inv.getDureeEnMois(); i++) {
            YearMonth month = startMonth.plusMonths(i + 1); // first earning is month 1 after start
            MonthlyEarning earning = new MonthlyEarning();
            earning.setInvestment(inv);
            earning.setMois(month.toString());
            earning.setMoisNumero(i + 1);
            earning.setMontantInteret(monthlyInterest);
            earning.setAccrued(false);
            schedule.add(earning);
        }
        monthlyEarningRepository.saveAll(schedule);
    }

    // ── Mapping helpers ────────────────────────────────────────────────────

    private void applyPlanDto(InvestmentPlan plan, InvestmentPlanCreateDTO dto) {
        if (dto.getMontantMin() >= dto.getMontantMax())
            throw new BadRequestException("Le montant minimum doit être inférieur au montant maximum.");
        plan.setNom(dto.getNom());
        plan.setDescription(dto.getDescription());
        plan.setDureeEnMois(dto.getDureeEnMois());
        plan.setTauxAnnuel(dto.getTauxAnnuel());
        plan.setMontantMin(dto.getMontantMin());
        plan.setMontantMax(dto.getMontantMax());
        plan.setActif(dto.isActif());
    }

    private InvestmentPlanResponseDTO mapPlan(InvestmentPlan p) {
        return InvestmentPlanResponseDTO.builder()
                .id(p.getId()).nom(p.getNom()).description(p.getDescription())
                .dureeEnMois(p.getDureeEnMois()).tauxAnnuel(p.getTauxAnnuel())
                .montantMin(p.getMontantMin()).montantMax(p.getMontantMax())
                .actif(p.isActif()).build();
    }

    private InvestmentResponseDTO mapInvestment(Investment inv) {
        LocalDateTime now = LocalDateTime.now();
        double progressPct   = 0;
        double currentValue  = inv.getMontant();
        long   daysRemaining = 0;
        double totalAccrued  = 0;

        if (inv.getStatut() == InvestmentStatut.ACTIVE) {
            long totalDays   = ChronoUnit.DAYS.between(inv.getDateDebut(), inv.getDateEcheance());
            long elapsedDays = ChronoUnit.DAYS.between(inv.getDateDebut(), now);
            progressPct = totalDays > 0
                    ? Math.min(100.0, Math.round((double) elapsedDays / totalDays * 10000.0) / 100.0)
                    : 0;
            totalAccrued = monthlyEarningRepository.totalAccruedByInvestment(inv.getId());
            currentValue = inv.getMontant() + totalAccrued;
            daysRemaining = Math.max(0, ChronoUnit.DAYS.between(now, inv.getDateEcheance()));
        } else if (inv.getStatut() == InvestmentStatut.COMPLETED) {
            progressPct   = 100.0;
            currentValue  = inv.getMontant() + inv.getInteretVerse();
            totalAccrued  = inv.getInteretVerse();
        }

        // Monthly breakdown
        List<MonthlyEarningDTO> breakdown = monthlyEarningRepository
                .findByInvestment_IdOrderByMoisNumeroAsc(inv.getId())
                .stream()
                .map(e -> MonthlyEarningDTO.builder()
                        .moisNumero(e.getMoisNumero())
                        .mois(e.getMois())
                        .moisLabel(formatMoisLabel(e.getMois()))
                        .montantInteret(e.getMontantInteret())
                        .accrued(e.isAccrued())
                        .build())
                .toList();

        return InvestmentResponseDTO.builder()
                .id(inv.getId())
                .compteId(inv.getCompte().getIdCompte())
                .planId(inv.getPlan().getId())
                .planNom(inv.getPlanNom())
                .montant(inv.getMontant())
                .tauxAnnuel(inv.getTauxAnnuel())
                .dureeEnMois(inv.getDureeEnMois())
                .interetAttendu(inv.getInteretAttendu())
                .interetVerse(inv.getInteretVerse())
                .dateDebut(inv.getDateDebut())
                .dateEcheance(inv.getDateEcheance())
                .dateCloture(inv.getDateCloture())
                .statut(inv.getStatut().name())
                .progressPct(progressPct)
                .currentValue(currentValue)
                .daysRemaining(daysRemaining)
                .totalAccrued(totalAccrued)
                .monthlyBreakdown(breakdown)
                .build();
    }

    private String formatMoisLabel(String mois) {
        // "2025-03" → "Mars 2025"
        try {
            YearMonth ym = YearMonth.parse(mois);
            return ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH)
                    .substring(0, 1).toUpperCase()
                    + ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH).substring(1)
                    + " " + ym.getYear();
        } catch (Exception e) {
            return mois;
        }
    }
}