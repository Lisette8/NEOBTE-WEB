import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../Services/admin-service';
import { UserListDTO } from '../../../Entities/DTO/Admin/user-list-dto';
import { UserCreateDTO } from '../../../Entities/DTO/Admin/user-create-dto';
import { UserUpdateDTO } from '../../../Entities/DTO/Admin/user-update-dto';

@Component({
  selector: 'app-user-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.html',
  styleUrl: './user-management.css',
})
export class UserManagement implements OnInit {
  users: UserListDTO[] = [];
  loading = false;
  error = '';

  userForm: FormGroup;
  isEditMode = false;
  selectedUserId: number | null = null;
  showForm = false;

  constructor(private adminService: AdminService, private fb: FormBuilder) {
    this.userForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      age: [null],
      adresse: [''],
      job: [''],
      genre: ['']
    });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;

    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load users';
        this.loading = false;
      }
    });
  }

  openCreate() {
    this.showForm = true;
    this.isEditMode = false;
    this.selectedUserId = null;
    this.userForm.reset({
      email: '',
      nom: '',
      prenom: '',
      age: null,
      adresse: '',
      job: '',
      genre: ''
    });
  }

  editUser(user: UserListDTO) {
    this.showForm = true;
    this.isEditMode = true;
    this.selectedUserId = user.idUtilisateur;

    this.userForm.patchValue({
      email: user.email,
      nom: user.nom,
      prenom: user.prenom,
      age: user.age ?? null,
      adresse: user.adresse ?? '',
      job: user.job ?? '',
      genre: user.genre ?? ''
    });
  }

  saveUser() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const value = this.userForm.value as any;

    if (this.isEditMode && this.selectedUserId != null) {
      const update: UserUpdateDTO = {
        email: value.email,
        nom: value.nom,
        prenom: value.prenom,
        age: value.age,
        adresse: value.adresse,
        job: value.job,
        genre: value.genre
      };

      this.adminService.updateUser(this.selectedUserId, update).subscribe({
        next: () => {
          this.loadUsers();
          this.resetForm();
        }
      });
    } else {
      const create: UserCreateDTO = {
        email: value.email,
        nom: value.nom,
        prenom: value.prenom,
        age: value.age,
        adresse: value.adresse,
        job: value.job,
        genre: value.genre
      };

      this.adminService.createUser(create).subscribe({
        next: () => {
          this.loadUsers();
          this.resetForm();
        }
      });
    }
  }

  deleteUser(id: number) {
    if (!confirm('Delete this user?')) return;

    this.adminService.deleteUser(id).subscribe({
      next: () => {
        this.loadUsers();
      }
    });
  }

  resetForm() {
    this.showForm = false;
    this.isEditMode = false;
    this.selectedUserId = null;
    this.userForm.reset({
      email: '',
      nom: '',
      prenom: '',
      age: null,
      adresse: '',
      job: '',
      genre: ''
    });
  }
}
