import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActualiteService } from '../../../Services/actualite-service';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Compte } from '../../../Entities/Interfaces/compte';
import { DemandeCompte } from '../../../Entities/Interfaces/demande-compte';
import { AuthService } from '../../../Services/auth-service';
import { CompteService } from '../../../Services/compte-service';
import { ClientAiService } from '../../../Services/client-ai.service';
import { ClientInsights } from '../../client-insights/client-insights';
import { ClientChatbot } from '../../client-chatbot/client-chatbot';
import { ClientMarketRates } from '../../client-market-rates/client-market-rates';
import { ClientInsightsData, PremiumStatus } from '../../../Entities/Interfaces/client-premium';
import { AccountPhysicalCard } from '../../account-physical-card/account-physical-card';
import { interval, Subscription } from 'rxjs';



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

  private accountsLoaded = false;
  private premiumLoaded = false;
  private pollSub?: Subscription;

  constructor(
    private authService: AuthService,
    private compteService: CompteService,
    private actualiteService: ActualiteService,
    private clientAiService: ClientAiService,
    private router: Router
  ) { }

  ngOnInit() {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.loadData(userId);
    this.pollSub = interval(180000).subscribe(() => this.loadData(userId));
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

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

  get visibleComptes(): Compte[] {
    return this.comptes.filter(c =>
      c.statutCompte !== 'CLOTURE' &&
      !(c.statutCompte === 'SUSPENDU' && c.dateSuppressionPrevue)
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

  get transferUsageLabel(): string {
    if (!this.premiumStatus) return '';
    return `${this.premiumStatus.transfersThisMonth}/${this.premiumStatus.monthlyLimit} transfers used`;
  }

  get transferUsagePct(): number {
    if (!this.premiumStatus?.monthlyLimit) return 0;
    return Math.min(100, Math.round((this.premiumStatus.transfersThisMonth / this.premiumStatus.monthlyLimit) * 100));
  }

  openAccount(compteId: number) { this.router.navigate(['/account', compteId]); }
  openPricing() { this.router.navigate(['/pricing-view']); }

  goToAiSection() {
    if (!this.isPremium) { this.openPricing(); return; }
    const el = document.getElementById('premium-section');
    el?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  getAccountLabel(type: string): string {
    switch (type) {
      case 'COURANT': return 'Compte Chèque';
      case 'EPARGNE': return 'Compte Épargne';
      case 'PROFESSIONNEL': return 'Compte Pro';
      default: return type;
    }
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
}