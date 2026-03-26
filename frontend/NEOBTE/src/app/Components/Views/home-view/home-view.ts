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
import { ClientChatbot } from '../../client-chatbot/client-chatbot';
import { ClientMarketRates } from '../../client-market-rates/client-market-rates';
import { AccountUsage, ClientInsightsData, PremiumStatus } from '../../../Entities/Interfaces/client-premium';
import { AccountPhysicalCard } from '../../account-physical-card/account-physical-card';
import { interval, Subscription } from 'rxjs';
import { Investment } from '../../../Entities/Interfaces/investment';
import { InvestmentService } from '../../../Services/investment-service';

@Component({
  selector: 'app-home-view',
  standalone: true,
  imports: [CommonModule, RouterLink, ClientInsights, ClientChatbot, ClientMarketRates, AccountPhysicalCard],
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
    private investmentService: InvestmentService
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

  private tryLoadInsights() {
    if (!this.premiumLoaded || !this.accountsLoaded) return;
    if (!this.isPremium) return;
    if (this.insightsLoading || this.insights) return;
    const accountIds = this.visibleComptes.map(c => c.idCompte);
    this.insightsLoading = true;
    this.insightsError = '';
    this.clientAiService.getClientInsights(accountIds, this.totalBalance).subscribe({
      next: (data) => { this.insights = data; this.insightsLoading = false; },
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

}