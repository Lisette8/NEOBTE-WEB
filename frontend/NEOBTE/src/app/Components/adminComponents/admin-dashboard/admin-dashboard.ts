import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { UserManagement } from '../user-management/user-management';
import { AdminSupport } from '../admin-support/admin-support';
import { ActualiteManagement } from '../actualite-management/actualite-management';
import { CompteManagement } from '../compte-management/compte-management';
import { VirementManagement } from '../virement-management/virement-management';
import { DemandeManagement } from '../demande-management/demande-management';
import { TreasuryComponent } from '../treasury-component/treasury-component';


@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, UserManagement, AdminSupport, ActualiteManagement,
            CompteManagement, VirementManagement, DemandeManagement, TreasuryComponent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard {
  selectedTab = 'demandes';
  today = new Date();

  tabTitles: Record<string, string> = {
    demandes:  'Account Requests',
    users:     'User Management',
    comptes:   'Account Management',
    virements: 'Transfer Management',
    support:   'Support Tickets',
    news:      'Actualités',
    treasury:  'Treasury',
  };

  tabSubtitles: Record<string, string> = {
    demandes:  'Review and approve or reject client account opening requests',
    users:     'Manage registered users and their roles',
    comptes:   'View and manage all client bank accounts',
    virements: 'Monitor and review all transfer operations',
    support:   'Handle client support requests and inquiries',
    news:      'Publish and manage news articles for clients',
    treasury:  'View collected service fees and transaction audit trail',
  };
}