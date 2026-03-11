import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { UserManagement } from '../../Views/user-management/user-management';
import { AdminSupport } from '../admin-support/admin-support';
import { ActualiteManagement } from '../../adminComponents/actualite-management/actualite-management';


@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, UserManagement, AdminSupport, ActualiteManagement],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard {
  selectedTab = 'users';
}
