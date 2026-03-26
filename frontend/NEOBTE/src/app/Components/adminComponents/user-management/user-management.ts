import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../Services/admin-service';
import { UserListDTO } from '../../../Entities/DTO/Admin/user-list-dto';
import { UserCreateDTO } from '../../../Entities/DTO/Admin/user-create-dto';
import { UserUpdateDTO } from '../../../Entities/DTO/Admin/user-update-dto';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-user-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.html',
  styleUrl: './user-management.css',
})
export class UserManagement implements OnInit, OnDestroy {

  private pollSub?: Subscription;

  users: UserListDTO[] = [];
  loading = false;
  error = '';

  currentTab: 'ADMINS' | 'CLIENTS' = 'CLIENTS';

  userForm: FormGroup;
  isEditMode = false;
  selectedUserId: number | null = null;
  showForm = false;

  constructor(
    private adminService: AdminService,
    private fb: FormBuilder,
    private modalService: ConfirmModalService
  ) {
    this.userForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      username: ['', Validators.required],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      cin: [''],
      telephone: [''],
      dateNaissance: [''],
      job: [''],
      genre: [''],
      adresse: [''],
      codePostal: [''],
      pays: ['Tunisie'],
    });
  }

  ngOnInit(): void {
    this.loadUsers();
    this.startPolling();
  }

  loadUsers() {
    this.loading = true;
    this.adminService.getAllUsers().subscribe({
      next: (data) => { this.users = data; this.loading = false; },
      error: () => { this.error = 'Failed to load users'; this.loading = false; }
    });
  }

  get filteredAdmins(): UserListDTO[] {
    return this.users.filter(u => u.role === 'ADMIN');
  }

  get filteredClients(): UserListDTO[] {
    return this.users.filter(u => u.role === 'CLIENT');
  }

  switchTab(tab: 'ADMINS' | 'CLIENTS') {
    this.currentTab = tab;
    this.showForm = false;
  }

  editUser(user: UserListDTO) {
    this.showForm = true;
    this.isEditMode = true;
    this.selectedUserId = user.id;

    this.userForm.patchValue({
      email: user.email,
      username: user.username ?? '',
      nom: user.nom,
      prenom: user.prenom,
      cin: user.cin ?? '',
      telephone: user.telephone ?? '',
      dateNaissance: user.dateNaissance ?? '',
      job: user.job ?? '',
      genre: user.genre ?? '',
      adresse: user.adresse ?? '',
      codePostal: user.codePostal ?? '',
      pays: user.pays ?? 'Tunisie',
    });
  }

  saveUser() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const v = this.userForm.value;

    if (this.isEditMode && this.selectedUserId != null) {
      const update: UserUpdateDTO = {
        nom: v.nom,
        prenom: v.prenom,
        telephone: v.telephone,
        dateNaissance: v.dateNaissance,
        job: v.job,
        genre: v.genre,
        adresse: v.adresse,
        codePostal: v.codePostal,
        pays: v.pays,
      };
      this.adminService.updateUser(this.selectedUserId, update).subscribe({
        next: () => { this.loadUsers(); this.resetForm(); }
      });
    } else {
      // Generate a random temporary password — user must change it on first login
      const tempPassword = 'Tmp@' + Math.random().toString(36).slice(2, 10);
      const create: UserCreateDTO = {
        email: v.email,
        username: v.username,
        nom: v.nom,
        prenom: v.prenom,
        cin: v.cin,
        telephone: v.telephone,
        dateNaissance: v.dateNaissance,
        job: v.job,
        genre: v.genre,
        adresse: v.adresse,
        codePostal: v.codePostal,
        pays: v.pays,
        motDePasse: tempPassword,
      };
      this.adminService.createUser(create).subscribe({
        next: () => { this.loadUsers(); this.resetForm(); }
      });
    }
  }

  async deleteUser(id: number) {
    const confirmed = await this.modalService.confirm({
      title: 'Delete User',
      message: 'Are you sure you want to delete this user? This action cannot be undone.',
      confirmText: 'Delete',
      cancelText: 'Cancel',
      type: 'danger'
    });

    if (confirmed) {
      this.adminService.deleteUser(id).subscribe({
        next: () => this.loadUsers()
      });
    }
  }

  resetForm() {
    this.showForm = false;
    this.isEditMode = false;
    this.selectedUserId = null;
    this.userForm.reset({ pays: 'Tunisie' });
  }

  // ── Input sanitizers ───────────────────────────────────────────────────────

  sanitizePhone(event: Event) {
    const input = event.target as HTMLInputElement;
    let val = input.value.replace(/[^\d+]/g, '');
    if (val.indexOf('+') > 0) val = val.replace(/\+/g, '');
    input.value = val;
    this.userForm.get('telephone')?.setValue(val, { emitEvent: false });
  }

  sanitizeDigits(event: Event, field: string) {
    const input = event.target as HTMLInputElement;
    const val = input.value.replace(/\D/g, '');
    input.value = val;
    this.userForm.get(field)?.setValue(val, { emitEvent: false });
  }

  togglePremium(user: UserListDTO) {
    const nextPremium = !(user.premium ?? false);
    // Optimistic UI update
    user.premium = nextPremium;
    this.adminService.setPremium(user.id, nextPremium).subscribe({
      next: (res) => {
        user.premium = res.premium;
      },
      error: () => {
        // Revert on failure
        user.premium = !nextPremium;
        this.error = 'Failed to update premium status';
      }
    });
  }

  startPolling() {
    this.pollSub = interval(60000).subscribe(() => {
      this.loadUsers();
    });
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

}

