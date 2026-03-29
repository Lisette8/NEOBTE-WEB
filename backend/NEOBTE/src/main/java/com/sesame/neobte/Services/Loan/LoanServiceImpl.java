package com.sesame.neobte.Services.Loan;

import com.sesame.neobte.DTO.Requests.Loan.LoanProductFormDTO;
import com.sesame.neobte.DTO.Requests.Loan.LoanRequestDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanProductResponseDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanRepaymentResponseDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.Loan.Loan;
import com.sesame.neobte.Entities.Class.Loan.LoanProduct;
import com.sesame.neobte.Entities.Class.Loan.LoanRepayment;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Loan.LoanStatut;
import com.sesame.neobte.Entities.Enumeration.Loan.RepaymentStatut;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.ICompteInterneRepository;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.Loan.ILoanProductRepository;
import com.sesame.neobte.Repositories.Loan.ILoanRepaymentRepository;
import com.sesame.neobte.Repositories.Loan.ILoanRepository;
import com.sesame.neobte.Services.Investment.InvestmentServiceImpl;
import com.sesame.neobte.Services.NotificationService;
import com.sesame.neobte.Services.Other.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    public static final String ACCOUNT_LOANS = "NEOBTE_LOANS";

    private final ILoanProductRepository productRepository;
    private final ILoanRepository loanRepository;
    private final ILoanRepaymentRepository repaymentRepository;
    private final ICompteRepository compteRepository;
    private final ICompteInterneRepository compteInterneRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    // ── Products (admin) ──────────────────────────────────────────────────

    @Override public List<LoanProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapProduct).toList();
    }
    @Override public List<LoanProductResponseDTO> getActiveProducts() {
        return productRepository.findByActifTrueOrderByDureeEnMoisAsc().stream().map(this::mapProduct).toList();
    }

    @Override @Transactional
    public LoanProductResponseDTO createProduct(LoanProductFormDTO dto) {
        LoanProduct p = new LoanProduct(); applyProductDto(p, dto);
        return mapProduct(productRepository.save(p));
    }
    @Override @Transactional
    public LoanProductResponseDTO updateProduct(Long id, LoanProductFormDTO dto) {
        LoanProduct p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit de prêt introuvable"));
        applyProductDto(p, dto);
        return mapProduct(productRepository.save(p));
    }
    @Override @Transactional
    public void deleteProduct(Long id) {
        LoanProduct p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit de prêt introuvable"));
        boolean hasActive = loanRepository.findByStatutOrderByDateCreationDesc(LoanStatut.ACTIVE)
                .stream().anyMatch(l -> l.getProduct().getId().equals(id));
        if (hasActive) { p.setActif(false); productRepository.save(p); }
        else productRepository.delete(p);
    }

    // ── Client: request a loan ────────────────────────────────────────────

    @Override @Transactional
    public LoanResponseDTO requestLoan(LoanRequestDTO dto, Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // Block restricted users (defaulted on a previous loan)
        if (user.isLoanRestricted())
            throw new BadRequestException(
                    "Votre accès aux prêts a été suspendu suite à des impayés. " +
                            "Contactez le support pour régulariser votre situation.");

        // One active loan at a time
        if (loanRepository.countActiveLoansByUser(userId) > 0)
            throw new BadRequestException(
                    "Vous avez déjà un prêt actif ou en cours d'approbation. " +
                            "Remboursez-le intégralement avant d'en demander un nouveau.");

        LoanProduct product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit de prêt introuvable"));
        if (!product.isActif())
            throw new BadRequestException("Ce produit de prêt n'est plus disponible.");
        if (dto.getMontant() < product.getMontantMin())
            throw new BadRequestException(String.format("Montant minimum : %.3f TND.", product.getMontantMin()));
        if (dto.getMontant() > product.getMontantMax())
            throw new BadRequestException(String.format("Montant maximum : %.3f TND.", product.getMontantMax()));

        Compte dest = compteRepository.findById(dto.getCompteDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination introuvable"));
        Compte prel = compteRepository.findById(dto.getComptePrelevementId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte de prélèvement introuvable"));

        if (!dest.getUtilisateur().getIdUtilisateur().equals(userId))
            throw new BadRequestException("Le compte de destination ne vous appartient pas.");
        if (!prel.getUtilisateur().getIdUtilisateur().equals(userId))
            throw new BadRequestException("Le compte de prélèvement ne vous appartient pas.");
        if (dest.getStatutCompte() != StatutCompte.ACTIVE || prel.getStatutCompte() != StatutCompte.ACTIVE)
            throw new BadRequestException("Les deux comptes doivent être actifs.");

        // Calculate annuity (reducing-balance formula)
        double mensualite = calcMensualite(dto.getMontant(), product.getTauxAnnuel(), product.getDureeEnMois());
        double totalDu    = Math.round(mensualite * product.getDureeEnMois() * 1000.0) / 1000.0;
        double totalInteret = Math.round((totalDu - dto.getMontant()) * 1000.0) / 1000.0;

        Loan loan = new Loan();
        loan.setUtilisateur(user);
        loan.setCompteDestination(dest);
        loan.setComptePrelevement(prel);
        loan.setProduct(product);
        loan.setProductNom(product.getNom());
        loan.setType(product.getType());
        loan.setMontant(dto.getMontant());
        loan.setTauxAnnuel(product.getTauxAnnuel());
        loan.setDureeEnMois(product.getDureeEnMois());
        loan.setMensualite(mensualite);
        loan.setTotalDu(totalDu);
        loan.setTotalInteret(totalInteret);
        loan.setStatut(LoanStatut.PENDING_APPROVAL);
        Loan saved = loanRepository.save(loan);

        log.info("[LOAN] User {} requested {} TND — product '{}' {} months",
                userId, dto.getMontant(), product.getNom(), product.getDureeEnMois());

        notificationService.notifyUser(userId, NotificationType.LOAN_REQUESTED,
                "Demande de prêt soumise",
                String.format("Votre demande de prêt de %.3f TND (« %s ») est en cours d'examen.",
                        dto.getMontant(), product.getNom()),
                "/loan-view");

        return mapLoan(saved);
    }

    // ── Admin: approve ────────────────────────────────────────────────────

    @Override @Transactional
    public LoanResponseDTO approveLoan(Long loanId, String adminNote) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prêt introuvable"));
        if (loan.getStatut() != LoanStatut.PENDING_APPROVAL)
            throw new BadRequestException("Ce prêt n'est pas en attente d'approbation.");

        // Treasury liquidity check — deployed capital must cover the loan
        double deployed = compteInterneRepository
                .findByNom(InvestmentServiceImpl.ACCOUNT_DEPLOYED)
                .map(c -> c.getSolde()).orElse(0.0);
        if (deployed < loan.getMontant())
            throw new BadRequestException(String.format(
                    "Liquidité insuffisante dans le pool déployé. Disponible : %.3f TND. Requis : %.3f TND.",
                    deployed, loan.getMontant()));

        // Move funds: DEPLOYED → client account
        debitInternal(InvestmentServiceImpl.ACCOUNT_DEPLOYED, loan.getMontant());
        creditInternal(ACCOUNT_LOANS, loan.getMontant()); // track total lent

        Compte dest = compteRepository.findByIdForUpdate(loan.getCompteDestination().getIdCompte()).orElseThrow();
        dest.setSolde(dest.getSolde() + loan.getMontant());
        compteRepository.save(dest);

        loan.setStatut(LoanStatut.ACTIVE);
        loan.setAdminNote(adminNote);
        loan.setDateApprobation(LocalDateTime.now());
        loan.setDateDisbursement(LocalDateTime.now());
        loan.setFundsReleased(true);

        // Pre-generate repayment schedule
        generateRepaymentSchedule(loan);
        loanRepository.save(loan);

        log.info("[LOAN] Admin approved loan {} — {} TND disbursed to account {}",
                loanId, loan.getMontant(), dest.getIdCompte());

        notificationService.notifyUser(loan.getUtilisateur().getIdUtilisateur(),
                NotificationType.LOAN_APPROVED,
                "Prêt approuvé !",
                String.format("Votre prêt de %.3f TND a été approuvé et crédité sur votre compte. " +
                                "Première mensualité le %s.",
                        loan.getMontant(),
                        LocalDate.now().plusMonths(1)),
                "/loan-view");

        return mapLoan(loan);
    }

    // ── Admin: reject ─────────────────────────────────────────────────────

    @Override @Transactional
    public LoanResponseDTO rejectLoan(Long loanId, String motif) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prêt introuvable"));
        if (loan.getStatut() != LoanStatut.PENDING_APPROVAL)
            throw new BadRequestException("Ce prêt n'est pas en attente d'approbation.");
        loan.setStatut(LoanStatut.REJECTED);
        loan.setMotifRejet(motif);
        loanRepository.save(loan);

        notificationService.notifyUser(loan.getUtilisateur().getIdUtilisateur(),
                NotificationType.LOAN_REJECTED,
                "Demande de prêt refusée",
                String.format("Votre demande de prêt de %.3f TND a été refusée. Motif : %s",
                        loan.getMontant(), motif),
                "/loan-view");

        return mapLoan(loan);
    }

    // ── Reads ─────────────────────────────────────────────────────────────

    @Override public List<LoanResponseDTO> getMyLoans(Long userId) {
        return loanRepository.findByUtilisateur_IdUtilisateurOrderByDateCreationDesc(userId)
                .stream().map(this::mapLoan).toList();
    }
    @Override public LoanResponseDTO getLoanById(Long loanId, Long userId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prêt introuvable"));
        if (!loan.getUtilisateur().getIdUtilisateur().equals(userId))
            throw new BadRequestException("Ce prêt ne vous appartient pas.");
        return mapLoan(loan);
    }
    @Override public List<LoanResponseDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .sorted((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()))
                .map(this::mapLoan).toList();
    }

    // ── Scheduler: collect monthly repayments (daily at 08:00) ───────────

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processMonthlyRepayments() {
        LocalDate today = LocalDate.now();
        List<LoanRepayment> due = repaymentRepository.findDueRepayments(today);
        if (due.isEmpty()) return;
        log.info("[LOAN] Processing {} due repayment(s)", due.size());
        for (LoanRepayment r : due) {
            try { collectRepayment(r); }
            catch (Exception e) { log.error("[LOAN] Repayment {} failed: {}", r.getId(), e.getMessage()); }
        }
    }

    private void collectRepayment(LoanRepayment r) {
        Loan loan = r.getLoan();
        Compte account = compteRepository.findByIdForUpdate(loan.getComptePrelevement().getIdCompte()).orElseThrow();

        if (account.getSolde() >= r.getMontantDu()) {
            // Successful collection
            account.setSolde(account.getSolde() - r.getMontantDu());
            compteRepository.save(account);

            // Repayment goes back into DEPLOYED capital (bank gets its money back)
            creditInternal(InvestmentServiceImpl.ACCOUNT_DEPLOYED, r.getPrincipalPortion());
            // Interest portion goes to FEES (bank revenue)
            creditInternal(InvestmentServiceImpl.ACCOUNT_FEES, r.getInteretPortion());

            r.setStatut(RepaymentStatut.PAID);
            r.setMontantPaye(r.getMontantDu());
            r.setDatePaiement(LocalDateTime.now());
            repaymentRepository.save(r);

            loan.setTotalRembourse(loan.getTotalRembourse() + r.getMontantDu());

            // Check if fully paid off
            long remaining = repaymentRepository.findByLoan_IdOrderByInstallmentNumberAsc(loan.getId())
                    .stream().filter(p -> p.getStatut() == RepaymentStatut.PENDING).count();
            if (remaining == 0) {
                loan.setStatut(LoanStatut.PAID_OFF);
                loan.setDateCloture(LocalDateTime.now());
                notificationService.notifyUser(loan.getUtilisateur().getIdUtilisateur(),
                        NotificationType.LOAN_PAID_OFF,
                        "Prêt entièrement remboursé !",
                        String.format("Félicitations ! Votre prêt de %.3f TND est entièrement remboursé.",
                                loan.getMontant()),
                        "/loan-view");
                log.info("[LOAN] Loan {} fully paid off", loan.getId());
            } else if (loan.getStatut() == LoanStatut.LATE) {
                loan.setStatut(LoanStatut.ACTIVE); // restore if was late
            }
            loanRepository.save(loan);
        } else {
            // Insufficient balance — mark as LATE (within grace period, no penalty yet)
            r.setStatut(RepaymentStatut.LATE);
            r.setRetryCount(r.getRetryCount() + 1);
            repaymentRepository.save(r);

            if (loan.getStatut() == LoanStatut.ACTIVE)
                loan.setStatut(LoanStatut.LATE);
            loanRepository.save(loan);

            notificationService.notifyUser(loan.getUtilisateur().getIdUtilisateur(),
                    NotificationType.LOAN_PAYMENT_FAILED,
                    "Échéance de prêt non honorée",
                    String.format("Votre mensualité de %.3f TND n'a pas pu être prélevée. " +
                                    "Approvisionnez votre compte sous %d jour(s) pour éviter une pénalité.",
                            r.getMontantDu(), loan.getProduct().getGracePeriodDays()),
                    "/loan-view");
        }
    }

    // ── Scheduler: apply penalties after grace period (daily at 09:00) ───

    @Override
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void applyPenalties() {
        // Find all repayments past their grace period
        // We query each active loan's product for gracePeriodDays
        List<LoanRepayment> all = repaymentRepository.findAll().stream()
                .filter(r -> (r.getStatut() == RepaymentStatut.LATE || r.getStatut() == RepaymentStatut.PENDING)
                        && !r.isPenaltyApplied()
                        && r.getDateDue().isBefore(LocalDate.now().minusDays(
                        r.getLoan().getProduct().getGracePeriodDays())))
                .toList();

        if (all.isEmpty()) return;
        log.info("[LOAN] Applying penalties to {} overdue repayment(s)", all.size());

        for (LoanRepayment r : all) {
            try { applyPenalty(r); }
            catch (Exception e) { log.error("[LOAN] Penalty application failed for {}: {}", r.getId(), e.getMessage()); }
        }
    }

    private void applyPenalty(LoanRepayment r) {
        Loan loan = r.getLoan();
        LoanProduct product = loan.getProduct();

        double penalty = Math.round(
                (r.getMontantDu() * product.getPenaltyRate() + product.getPenaltyFixedFee()) * 1000.0) / 1000.0;

        r.setPenalite(penalty);
        r.setPenaltyApplied(true);
        r.setStatut(RepaymentStatut.FAILED);
        repaymentRepository.save(r);

        loan.setTotalPenalites(loan.getTotalPenalites() + penalty);
        loan.setMissedPayments(loan.getMissedPayments() + 1);

        // Escalate to DEFAULT after threshold missed payments
        if (loan.getMissedPayments() >= product.getDefaultThreshold()) {
            loan.setStatut(LoanStatut.DEFAULT);

            // Restrict user from new loans
            Utilisateur user = loan.getUtilisateur();
            user.setLoanRestricted(true);
            utilisateurRepository.save(user);

            log.warn("[LOAN] Loan {} moved to DEFAULT — user {} restricted", loan.getId(),
                    user.getIdUtilisateur());

            notificationService.notifyUser(user.getIdUtilisateur(),
                    NotificationType.LOAN_DEFAULT,
                    "Prêt en défaut de paiement",
                    String.format("Votre prêt de %.3f TND est en défaut après %d mensualités impayées. " +
                                    "Votre accès aux nouveaux prêts est suspendu. Contactez le support immédiatement.",
                            loan.getMontant(), loan.getMissedPayments()),
                    "/loan-view");
        } else {
            notificationService.notifyUser(loan.getUtilisateur().getIdUtilisateur(),
                    NotificationType.LOAN_PENALTY,
                    "Pénalité de retard appliquée",
                    String.format("Une pénalité de %.3f TND a été ajoutée à votre prêt pour " +
                                    "l'échéance #%d impayée. Total pénalités : %.3f TND.",
                            penalty, r.getInstallmentNumber(), loan.getTotalPenalites()),
                    "/loan-view");
        }

        loanRepository.save(loan);
        log.info("[LOAN] Penalty %.3f TND applied to repayment {} (loan {})", penalty, r.getId(), loan.getId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Standard annuity (reducing-balance) monthly payment formula.
     * This is what all Tunisian banks use: M = P × [r(1+r)^n] / [(1+r)^n - 1]
     * where r = monthly rate = annualRate / 12
     */
    public static double calcMensualite(double principal, double annualRate, int months) {
        double r = annualRate / 12.0;
        if (r == 0) return Math.round(principal / months * 1000.0) / 1000.0;
        double factor = Math.pow(1 + r, months);
        return Math.round(principal * r * factor / (factor - 1) * 1000.0) / 1000.0;
    }

    private void generateRepaymentSchedule(Loan loan) {
        double principal   = loan.getMontant();
        double monthlyRate = loan.getTauxAnnuel() / 12.0;
        double mensualite  = loan.getMensualite();
        double balance     = principal;
        List<LoanRepayment> schedule = new ArrayList<>();

        for (int i = 1; i <= loan.getDureeEnMois(); i++) {
            double interetPortion    = Math.round(balance * monthlyRate * 1000.0) / 1000.0;
            double principalPortion  = Math.round((mensualite - interetPortion) * 1000.0) / 1000.0;
            // Last installment — adjust for rounding
            if (i == loan.getDureeEnMois()) principalPortion = Math.round(balance * 1000.0) / 1000.0;

            LoanRepayment r = new LoanRepayment();
            r.setLoan(loan);
            r.setInstallmentNumber(i);
            r.setDateDue(LocalDate.now().plusMonths(i));
            r.setMontantDu(mensualite);
            r.setPrincipalPortion(principalPortion);
            r.setInteretPortion(interetPortion);
            r.setStatut(RepaymentStatut.PENDING);
            schedule.add(r);

            balance = Math.max(0, balance - principalPortion);
        }
        repaymentRepository.saveAll(schedule);
    }

    private void applyProductDto(LoanProduct p, LoanProductFormDTO dto) {
        if (dto.getMontantMin() >= dto.getMontantMax())
            throw new BadRequestException("Le montant minimum doit être inférieur au montant maximum.");
        p.setNom(dto.getNom()); p.setDescription(dto.getDescription());
        p.setType(dto.getType()); p.setDureeEnMois(dto.getDureeEnMois());
        p.setTauxAnnuel(dto.getTauxAnnuel()); p.setMontantMin(dto.getMontantMin());
        p.setMontantMax(dto.getMontantMax()); p.setGracePeriodDays(dto.getGracePeriodDays());
        p.setPenaltyRate(dto.getPenaltyRate()); p.setPenaltyFixedFee(dto.getPenaltyFixedFee());
        p.setDefaultThreshold(dto.getDefaultThreshold()); p.setActif(dto.isActif());
    }

    private void creditInternal(String name, double amount) {
        var acc = compteInterneRepository.findByNomForUpdate(name)
                .orElseThrow(() -> new IllegalStateException("Compte interne introuvable: " + name));
        acc.setSolde(acc.getSolde() + amount);
        compteInterneRepository.save(acc);
    }

    private void debitInternal(String name, double amount) {
        var acc = compteInterneRepository.findByNomForUpdate(name)
                .orElseThrow(() -> new IllegalStateException("Compte interne introuvable: " + name));
        acc.setSolde(Math.max(0, acc.getSolde() - amount));
        compteInterneRepository.save(acc);
    }

    private LoanProductResponseDTO mapProduct(LoanProduct p) {
        double exMens = calcMensualite(p.getMontantMin(), p.getTauxAnnuel(), p.getDureeEnMois());
        return LoanProductResponseDTO.builder()
                .id(p.getId()).nom(p.getNom()).description(p.getDescription())
                .type(p.getType().name()).dureeEnMois(p.getDureeEnMois()).tauxAnnuel(p.getTauxAnnuel())
                .montantMin(p.getMontantMin()).montantMax(p.getMontantMax())
                .gracePeriodDays(p.getGracePeriodDays()).penaltyRate(p.getPenaltyRate())
                .penaltyFixedFee(p.getPenaltyFixedFee()).defaultThreshold(p.getDefaultThreshold())
                .actif(p.isActif()).exampleMensualite(exMens).build();
    }

    private LoanResponseDTO mapLoan(Loan l) {
        List<LoanRepaymentResponseDTO> repayments = repaymentRepository
                .findByLoan_IdOrderByInstallmentNumberAsc(l.getId())
                .stream().map(r -> LoanRepaymentResponseDTO.builder()
                        .id(r.getId()).installmentNumber(r.getInstallmentNumber())
                        .dateDue(r.getDateDue()).montantDu(r.getMontantDu())
                        .principalPortion(r.getPrincipalPortion()).interetPortion(r.getInteretPortion())
                        .penalite(r.getPenalite()).montantPaye(r.getMontantPaye())
                        .statut(r.getStatut().name()).datePaiement(r.getDatePaiement())
                        .penaltyApplied(r.isPenaltyApplied()).build())
                .toList();

        double resteADu = Math.max(0, l.getTotalDu() + l.getTotalPenalites() - l.getTotalRembourse());
        double progressPct = l.getTotalDu() > 0
                ? Math.min(100.0, Math.round(l.getTotalRembourse() / l.getTotalDu() * 10000.0) / 100.0)
                : 0;

        return LoanResponseDTO.builder()
                .id(l.getId())
                .compteDestinationId(l.getCompteDestination().getIdCompte())
                .comptePrelevementId(l.getComptePrelevement().getIdCompte())
                .productId(l.getProduct().getId()).productNom(l.getProductNom())
                .type(l.getType().name()).montant(l.getMontant()).tauxAnnuel(l.getTauxAnnuel())
                .dureeEnMois(l.getDureeEnMois()).mensualite(l.getMensualite())
                .totalDu(l.getTotalDu()).totalInteret(l.getTotalInteret())
                .totalRembourse(l.getTotalRembourse()).totalPenalites(l.getTotalPenalites())
                .resteADu(resteADu).missedPayments(l.getMissedPayments())
                .statut(l.getStatut().name()).motifRejet(l.getMotifRejet()).adminNote(l.getAdminNote())
                .dateCreation(l.getDateCreation()).dateApprobation(l.getDateApprobation())
                .dateDisbursement(l.getDateDisbursement()).dateCloture(l.getDateCloture())
                .progressPct(progressPct).repayments(repayments).build();
    }
}