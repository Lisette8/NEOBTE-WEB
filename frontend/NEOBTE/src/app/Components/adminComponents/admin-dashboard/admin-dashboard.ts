import { CommonModule, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { UserManagement } from '../user-management/user-management';
import { AdminSupport } from '../admin-support/admin-support';
import { ActualiteManagement } from '../../adminComponents/actualite-management/actualite-management';
import { CompteManagement } from '../compte-management/compte-management';
import { VirementManagement } from '../virement-management/virement-management';


@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, UserManagement, AdminSupport, ActualiteManagement, CompteManagement, VirementManagement],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})


export class AdminDashboard {
  selectedTab = 'users';
  today = new Date();

  tabTitles: Record<string, string> = {
    users: 'User Management',
    comptes: 'Account Management',
    virements: 'Transfer Management',
    support: 'Support Tickets',
    news: 'Actualités',
  };

  tabSubtitles: Record<string, string> = {
    users: 'Manage registered users and their roles',
    comptes: 'View and manage all client bank accounts',
    virements: 'Monitor and review all transfer operations',
    support: 'Handle client support requests and inquiries',
    news: 'Publish and manage news articles for clients',
  };
}
