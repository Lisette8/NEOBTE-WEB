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
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';


@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, UserManagement, AdminSupport, ActualiteManagement,
    CompteManagement, VirementManagement, DemandeManagement, TreasuryComponent,
    FraudeManagement, AiAnalytics],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit, OnDestroy {
  selectedTab = 'demandes';
  today = new Date();
  openAlertCount = 0;

  constructor(
    private fraudeService: FraudeService,
    private ws: WebsocketService
  ) { }

  ngOnInit() {
    this.loadAlertCount();
    // Keep the badge counter in the sidebar in sync in real time
    this.ws.subscribeAdmin((event) => {
      if (event.type === 'FRAUDE') this.loadAlertCount();
    });
  }

  ngOnDestroy() { }

  loadAlertCount() {
    this.fraudeService.countOpen().subscribe({
      next: (res) => this.openAlertCount = res.count,
      error: () => { }
    });
  }

  tabTitles: Record<string, string> = {
    demandes: 'Account Requests',
    users: 'User Management',
    comptes: 'Account Management',
    virements: 'Transfer Management',
    support: 'Support Tickets',
    news: 'Actualités',
    treasury: 'Treasury',
    security: 'Security & Fraud Detection',
    ai: 'AI Analytics',
  };

  tabSubtitles: Record<string, string> = {
    demandes: 'Review and approve or reject client account opening requests',
    users: 'Manage registered users and their roles',
    comptes: 'View and manage all client bank accounts',
    virements: 'Monitor and review all transfer operations',
    support: 'Handle client support requests and inquiries',
    news: 'Publish and manage news articles for clients',
    treasury: 'View collected service fees and transaction audit trail',
    security: 'Monitor fraud alerts, review suspicious activity and configure detection rules',
    ai: 'AI-powered fraud intelligence, financial forecasts, and risk insights — powered by Groq',
  };
}