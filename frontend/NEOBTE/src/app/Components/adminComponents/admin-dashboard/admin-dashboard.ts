import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { UserManagement } from '../user-management/user-management';
import { AdminSupport } from '../admin-support/admin-support';
import { ActualiteManagement } from '../actualite-management/actualite-management';
import { CompteManagement } from '../compte-management/compte-management';
import { VirementManagement } from '../virement-management/virement-management';
import { DemandeManagement } from '../demande-management/demande-management';
import { TreasuryComponent } from '../treasury-component/treasury-component';
import { FraudeManagement } from '../fraude-management/fraude-management';
import { FraudeService } from '../../../Security/Services/FraudeService';
import { AiAnalytics } from '../ai-analytics/ai-analytics';
import { interval } from 'rxjs/internal/observable/interval';
import { debounceTime, distinctUntilChanged, Subject, Subscription } from 'rxjs';
import { AdminService, AdminStats, GlobalSearchResult } from '../../../Services/admin-service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';


const VALID_TABS = ['home', 'demandes', 'users', 'comptes', 'virements', 'support', 'news', 'treasury', 'security', 'ai'];

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, UserManagement, AdminSupport, ActualiteManagement,
    CompteManagement, VirementManagement, DemandeManagement, TreasuryComponent,
    FraudeManagement, AiAnalytics],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit, OnDestroy {

  selectedTab = 'home';
  today = new Date();
  openAlertCount = 0;

  stats: AdminStats | null = null;
  statsLoading = true;

  searchQuery = '';
  searchResults: GlobalSearchResult | null = null;
  searching = false;
  private searchSubject = new Subject<string>();

  private pollSub?: Subscription;
  private searchSub?: Subscription;
  private routeSub?: Subscription;

  constructor(
    private fraudeService: FraudeService,
    private adminService: AdminService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.routeSub = this.route.queryParams.subscribe(params => {
      const tab = params['tab'];
      this.selectedTab = (tab && VALID_TABS.includes(tab)) ? tab : 'home';
    });

    this.loadAlertCount();
    this.loadStats();

    this.pollSub = interval(2000).subscribe(() => {
      this.loadAlertCount();
      if (this.selectedTab === 'home') this.loadStats();
    });

    this.searchSub = this.searchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged()
    ).subscribe(q => {
      if (!q.trim()) { this.searchResults = null; this.searching = false; return; }
      this.searching = true;
      this.adminService.globalSearch(q.trim()).subscribe({
        next: (r) => { this.searchResults = r; this.searching = false; },
        error: () => { this.searching = false; }
      });
    });
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
    this.searchSub?.unsubscribe();
    this.routeSub?.unsubscribe();
  }

  goTo(tab: string) {
    this.selectedTab = tab;
    this.router.navigate([], { queryParams: { tab }, replaceUrl: true });
    this.clearSearch();
  }

  loadAlertCount() {
    this.fraudeService.countOpen().subscribe({
      next: (res) => this.openAlertCount = res.count,
      error: () => { }
    });
  }

  loadStats() {
    this.adminService.getStats().subscribe({
      next: (s) => { this.stats = s; this.statsLoading = false; },
      error: () => { this.statsLoading = false; }
    });
  }

  onSearchInput(val: string) {
    this.searchQuery = val;
    this.searchSubject.next(val);
  }

  clearSearch() {
    this.searchQuery = '';
    this.searchResults = null;
    this.searching = false;
  }

  get hasResults(): boolean {
    if (!this.searchResults) return false;
    return (this.searchResults.users.length + this.searchResults.accounts.length +
      this.searchResults.transfers.length + this.searchResults.tickets.length) > 0;
  }

  get totalResults(): number {
    if (!this.searchResults) return 0;
    return this.searchResults.users.length + this.searchResults.accounts.length +
      this.searchResults.transfers.length + this.searchResults.tickets.length;
  }

  tabTitles: Record<string, string> = {
    home: 'Dashboard', demandes: 'Account Requests', users: 'User Management',
    comptes: 'Account Management', virements: 'Transfer Management', support: 'Support Tickets',
    news: 'Actualités', treasury: 'Treasury', security: 'Security & Fraud', ai: 'AI Analytics',
  };

  tabSubtitles: Record<string, string> = {
    home: 'Platform overview — stats, alerts and quick search',
    demandes: 'Review and approve or reject client account opening requests',
    users: 'Manage registered users and their roles',
    comptes: 'View and manage all client bank accounts',
    virements: 'Monitor and review all transfer operations',
    support: 'Handle client support requests and inquiries',
    news: 'Publish and manage news articles for clients',
    treasury: 'View collected service fees and transaction audit trail',
    security: 'Monitor fraud alerts, review suspicious activity and configure detection rules',
    ai: 'AI-powered fraud intelligence, financial forecasts, and risk insights',
  };



  fmt(n: number | undefined): string {
    if (n === undefined || n === null) return '—';
    return n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
  }
}