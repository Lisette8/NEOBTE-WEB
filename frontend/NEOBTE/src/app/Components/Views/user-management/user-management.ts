import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../Services/admin-service';
import { User } from '../../../Entities/Classes/user';

@Component({
  selector: 'app-user-management',
  imports: [CommonModule],
  templateUrl: './user-management.html',
  styleUrl: './user-management.css',
})
export class UserManagement implements OnInit{
  
  users: User[] = [];
  loading = false;
  error = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }


  
  //methodes

  loadUsers() {
    this.loading = true;

    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: () => {
        this.error = "Failed to load users";
        this.loading = false;
      }
    });
  }


  deleteUser(id: number) {
    if (!confirm("Delete this user?")) return;

    this.adminService.deleteUser(id).subscribe({
      next: () => {
        this.loadUsers();
      }
    });
  }
}
