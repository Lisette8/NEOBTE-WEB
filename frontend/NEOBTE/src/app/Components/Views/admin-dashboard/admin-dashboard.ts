import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { UserManagement } from '../user-management/user-management';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, UserManagement],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard {

}
