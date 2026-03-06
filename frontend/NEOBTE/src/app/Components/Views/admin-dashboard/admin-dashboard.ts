import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { UserManagement } from '../user-management/user-management';
import { AdminSupport } from '../admin-support/admin-support';


@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, UserManagement, AdminSupport],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard {
  selectedTab = 'users';
}
