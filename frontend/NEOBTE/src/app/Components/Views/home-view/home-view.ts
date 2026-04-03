import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActualiteService } from '../../../Services/actualite-service';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ACCOUNT_TYPE_META, Compte } from '../../../Entities/Interfaces/compte';
import { DemandeCompte } from '../../../Entities/Interfaces/demande-compte';
import { AuthService } from '../../../Services/auth-service';
import { CompteService } from '../../../Services/compte-service';
import { ClientAiService } from '../../../Services/client-ai.service';
import { ClientInsights } from '../../client-insights/client-insights';
import { AccountUsage, ClientInsightsData, PremiumStatus } from '../../../Entities/Interfaces/client-premium';
import { AccountPhysicalCard } from '../../account-physical-card/account-physical-card';
import { interval, Subscription } from 'rxjs';
import { Investment } from '../../../Entities/Interfaces/investment';
import { InvestmentService } from '../../../Services/investment.service';
import { NeoChart } from '../../neo-chart/neo-chart';
import { Loan } from '../../../Entities/Interfaces/loan';
import { Virement } from '../../../Entities/Interfaces/virement';
import { LoanService } from '../../../Services/loan.service';
import { VirementService } from '../../../Services/virement.service';



@Component({
  selector: 'app-home-view',
  standalone: true,
  imports: [CommonModule, RouterLink, ClientInsights, AccountPhysicalCard, NeoChart],
  templateUrl: './home-view.html',
  styleUrl: './home-view.css',
})
export class HomeView implements OnInit, OnDestroy {

  userName = '';
  comptes: Compte[] = [];
  demandes: DemandeCompte[] = [];
  actualites: Actualite[] = [];
  loading = true;

  premiumStatus: PremiumStatus | null = null;
  premiumLoading = true;
  premiumError = '';

  insights: ClientInsightsData | null = null;
  insightsLoading = false;
  insightsError = '';

  activeInvestments: Investment[] = [];
  activeLoans: Loan[] = [];
  recentVirements: Virement[] = [];
  private accountsLoaded = false;
  private premiumLoaded = false;
  private pollSub?: Subscription;

  readonly accountTypeMeta = ACCOUNT_TYPE_META;

  constructor(
    private authService: AuthService,
    private compteService: CompteService,
    private actualiteService: ActualiteService,
    private clientAiService: ClientAiService,
    private router: Router,
    private investmentService: InvestmentService,
    private loanService: LoanService,
    private virementService: VirementService
  ) { }

  ngOnInit() {
    const userId = this.authService.getUserId();
    if (!userId) return;
    this.loadData(userId);
    this.pollSub = interval(180000).subscribe(() => this.loadData(userId));
  }

  ngOnDestroy() { this.pollSub?.unsubscribe(); }

  private loadData(userId: number) {
    this.authService.getCurrentUser().subscribe({
      next: (user) => this.userName = user.prenom || '',
      error: () => { }
    });

    this.compteService.getUserAccounts(userId).subscribe({
      next: (data) => {
        this.comptes = data;
        this.loading = false;
        this.accountsLoaded = true;
        this.tryLoadInsights();
      },
      error: () => { this.loading = false; this.accountsLoaded = true; }
    });

    this.compteService.getMyDemandes().subscribe({
      next: (data) => this.demandes = data,
      error: () => { }
    });

    this.actualiteService.getAll(0, 3).subscribe({
      next: (data) => this.actualites = data.content,
      error: () => { }
    });

    this.investmentService.getMyInvestments().subscribe({
      next: (inv) => this.activeInvestments = inv.filter(i => i.statut === 'ACTIVE'),
      error: () => { }
    });

    this.loanService.getMyLoans().subscribe({
      next: (loans) => this.activeLoans = loans.filter(l => l.statut === 'ACTIVE' || l.statut === 'APPROVED'),
      error: () => { }
    });

    this.virementService.getHistory().subscribe({
      next: (v) => this.recentVirements = v,
      error: () => { }
    });

    this.clientAiService.getPremiumStatus().subscribe({
      next: (s) => {
        this.premiumStatus = s;
        this.premiumLoading = false;
        this.premiumLoaded = true;
        this.tryLoadInsights();
      },
      error: () => {
        this.premiumLoading = false;
        this.premiumLoaded = true;
        this.premiumError = 'Failed to load premium status.';
      }
    });
  }

  // ── Account getters ──────────────────────────────────────────────────────

  get visibleComptes(): Compte[] {
    return this.comptes.filter(c =>
      c.statutCompte !== 'CLOSED' &&
      !(c.statutCompte === 'SUSPENDED' && c.dateSuppressionPrevue)
    );
  }

  get totalBalance(): number {
    return this.visibleComptes
      .filter(c => c.statutCompte === 'ACTIVE')
      .reduce((sum, c) => sum + (c.solde ?? 0), 0);
  }

  get activeComptes(): Compte[] { return this.visibleComptes.filter(c => c.statutCompte === 'ACTIVE'); }
  get pendingDemandes(): DemandeCompte[] { return this.demandes.filter(d => d.statutDemande === 'EN_ATTENTE'); }
  get isPremium(): boolean { return !!this.premiumStatus?.premium; }

  /** True when the client already owns (or has pending demandes for) all 3 account types */
  get allAccountTypesTaken(): boolean {
    const ALL_TYPES = ['COURANT', 'EPARGNE', 'PROFESSIONNEL'];
    const taken = new Set([
      ...this.comptes.map(c => c.typeCompte),
      ...this.pendingDemandes.map(d => d.typeCompte),
    ]);
    return ALL_TYPES.every(t => taken.has(t));
  }

  // ── Per-account daily usage (replaces profile-based monthly quota) ────────

  /** Usage entries from the status endpoint, one per active account. */
  get accountUsages(): AccountUsage[] {
    return this.premiumStatus?.accountUsages ?? [];
  }

  /** Usage for a specific compte (matched by id). */
  usageFor(compteId: number): AccountUsage | undefined {
    return this.accountUsages.find(u => u.compteId === compteId);
  }

  /** Daily usage percentage for a specific account (0–100). */
  dailyUsagePct(usage: AccountUsage): number {
    if (!usage.dailyCountLimit) return 0;
    return Math.min(100, Math.round((usage.dailyCountUsed / usage.dailyCountLimit) * 100));
  }

  /** True when an account has used ≥ 80% of its daily transfer count. */
  isNearDailyLimit(usage: AccountUsage): boolean {
    return this.dailyUsagePct(usage) >= 80;
  }

  /** True when an account has reached its daily limit. */
  isAtDailyLimit(usage: AccountUsage): boolean {
    return usage.dailyCountUsed >= usage.dailyCountLimit;
  }

  // ── Savings interest helpers ──────────────────────────────────────────────

  get savingsBalance(): number {
    return this.activeComptes
      .filter(c => c.typeCompte === 'EPARGNE')
      .reduce((sum, c) => sum + (c.solde ?? 0), 0);
  }

  get hasSavingsAccount(): boolean {
    return this.activeComptes.some(c => c.typeCompte === 'EPARGNE');
  }

  get projectedAnnualInterest(): number {
    return Math.round(this.savingsBalance * 0.045 * 1000) / 1000;
  }

  // ── Navigation ────────────────────────────────────────────────────────────

  openAccount(compteId: number) { this.router.navigate(['/account', compteId]); }
  openPricing() { this.router.navigate(['/pricing-view']); }

  goToAiSection() {
    if (!this.isPremium) { this.openPricing(); return; }
    const el = document.getElementById('premium-section');
    el?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  getAccountLabel(type: string): string {
    return this.accountTypeMeta[type]?.label ?? type;
  }

  getTimeOfDay(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Bonjour';
    if (h < 18) return 'Bon après-midi';
    return 'Bonsoir';
  }

  get recentInvestments(): Investment[] {
    return [...this.activeInvestments]
      .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime())
      .slice(0, 5);
  }

  get recentActualites(): Actualite[] {
    return [...this.actualites]
      .sort((a, b) => new Date(b.dateCreationActualite).getTime() - new Date(a.dateCreationActualite).getTime())
      .slice(0, 5);
  }

  formatDateShort(iso: string): string {
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('fr-TN', { day: '2-digit', month: 'short' });
    } catch {
      return iso;
    }
  }

  // ── Chart (cached — computed once after data loads) ─────
  // balanceChartPoints: empty string = no data (controls *ngIf guard in template)
  balanceChartPoints = '';
  chartXLabels: string[] = [];
  chartValues: number[] = [];  // public — bound in template via [values]="chartValues"

  private buildChart(): void {
    const values = this.insights?.dailyBalance?.values ?? [];
    if (values.length < 2) {
      this.balanceChartPoints = '';
      this.chartValues = [];
      this.chartXLabels = [];
      return;
    }

    this.chartValues = values;
    this.balanceChartPoints = 'ok'; // truthy sentinel — activates *ngIf

    // X labels
    const n = values.length;
    const lbls = this.insights?.dailyBalance?.labels ?? [];
    const src = lbls.length === n ? lbls : values.map((_, i) => {
      const d = new Date();
      d.setDate(d.getDate() - (n - 1 - i));
      return d.toISOString().slice(0, 10);
    });
    this.chartXLabels = src.map((d: string) => {
      try { return new Date(d).toLocaleDateString('fr-TN', { day: '2-digit', month: 'short' }); }
      catch { return d; }
    });
  }

  private tryLoadInsights() {
    if (!this.premiumLoaded || !this.accountsLoaded) return;
    if (!this.isPremium) return;
    if (this.insightsLoading || this.insights) return;
    const accountIds = this.visibleComptes.map(c => c.idCompte);
    this.insightsLoading = true;
    this.insightsError = '';
    this.clientAiService.getClientInsights(accountIds, this.totalBalance).subscribe({
      next: (data) => {
        this.insights = data;
        this.insightsLoading = false;
        this.buildChart();  // compute chart data once after data arrives
      },
      error: () => { this.insightsLoading = false; this.insightsError = 'Failed to load insights.'; }
    });
  }
  get totalInvested(): number {
    return this.activeInvestments.reduce((s, i) => s + i.montant, 0);
  }

  get totalExpectedInterest(): number {
    return this.activeInvestments.reduce((s, i) => s + i.interetAttendu, 0);
  }

  /** The primary account: COURANT first, then PROFESSIONNEL, then EPARGNE */
  get primaryAccount(): Compte | null {
    const order = ['COURANT', 'PROFESSIONNEL', 'EPARGNE'];
    for (const type of order) {
      const found = this.activeComptes.find(c => c.typeCompte === type);
      if (found) return found;
    }
    return this.activeComptes[0] ?? null;
  }

  /** Whether the primary account can send external transfers */
  get canPrimarySendExternal(): boolean {
    const primary = this.primaryAccount;
    if (!primary) return false;
    return ACCOUNT_TYPE_META[primary.typeCompte]?.canSendExternal ?? true;
  }


  // ── Financial health getters ──────────────────────────────────────────────

  /** Total monthly loan repayments across active loans */
  get totalMensualites(): number {
    return this.activeLoans.reduce((s, l) => s + (l.mensualite ?? 0), 0);
  }

  /** Debt ratio: monthly repayments / total balance (0–1) */
  get debtRatio(): number {
    if (!this.totalBalance) return 0;
    return Math.min(1, this.totalMensualites / this.totalBalance);
  }

  /** 0=healthy, 1=warning, 2=critical */
  get debtRatioLevel(): 0 | 1 | 2 {
    if (this.debtRatio < 0.15) return 0;
    if (this.debtRatio < 0.35) return 1;
    return 2;
  }

  get debtRatioLabel(): string {
    if (this.debtRatioLevel === 0) return 'Sain';
    if (this.debtRatioLevel === 1) return 'Modéré';
    return 'Élevé';
  }

  /** Net flow this month: received - sent (from virement history) */
  get monthlyNetFlow(): number {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).getTime();
    const myAccountIds = new Set(this.comptes.map(c => c.idCompte));
    let received = 0;
    let sent = 0;
    for (const v of this.recentVirements) {
      const t = new Date(v.dateDeVirement).getTime();
      if (t < startOfMonth) continue;
      if (myAccountIds.has(v.compteDestinationId)) received += v.montant;
      if (myAccountIds.has(v.compteSourceId)) sent += (v.totalDebite ?? v.montant);
    }
    return received - sent;
  }

  get monthlyNetFlowPositive(): boolean { return this.monthlyNetFlow >= 0; }

  /** Accrued interest already earned across active placements */
  get totalAccruedInterest(): number {
    return this.activeInvestments.reduce((s, i) => s + (i.totalAccrued ?? 0), 0);
  }

  /** Next upcoming deadline across loans and investments */
  get nextDeadline(): { label: string; date: string; type: 'loan' | 'placement'; daysLeft: number } | null {
    const today = Date.now();
    const candidates: { label: string; date: string; type: 'loan' | 'placement'; daysLeft: number }[] = [];

    for (const l of this.activeLoans) {
      const next = l.repayments?.find(r => r.statut === 'PENDING');
      if (next?.dateDue) {
        const daysLeft = Math.ceil((new Date(next.dateDue).getTime() - today) / 86400000);
        if (daysLeft >= 0) candidates.push({ label: l.productNom ?? 'Prêt', date: next.dateDue, type: 'loan', daysLeft });
      }
    }

    for (const i of this.activeInvestments) {
      if (i.dateEcheance) {
        const daysLeft = Math.ceil((new Date(i.dateEcheance).getTime() - today) / 86400000);
        if (daysLeft >= 0) candidates.push({ label: i.planNom, date: i.dateEcheance, type: 'placement', daysLeft });
      }
    }

    if (!candidates.length) return null;
    return candidates.sort((a, b) => a.daysLeft - b.daysLeft)[0];
  }

  get hasFinancialHealth(): boolean {
    return this.activeComptes.length > 0;
  }

}