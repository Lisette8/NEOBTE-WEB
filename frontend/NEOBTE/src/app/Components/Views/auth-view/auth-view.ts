import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../../Services/auth-service';
import { LoginRequest } from '../../../Entities/Interfaces/login-request';
import { RegisterRequest } from '../../../Entities/Interfaces/register-request';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth-view',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './auth-view.html',
  styleUrl: './auth-view.css',
})
export class AuthView implements OnInit {
  isLoginMode = true;
  authForm: FormGroup;
  showPassword = false;
  error = '';
  loading = false;
 
  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.authForm = this.fb.group({
      email:      ['', [Validators.required, Validators.email]],
      motDePasse: ['', Validators.required],
      prenom:     [''],
      nom:        [''],
      telephone:  [''],
    });
  }
 
  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getUserRole();
      this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
    }
  }
 
  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.error = '';
    this.authForm.reset();
    this.updateValidators();
  }
 
  private updateValidators() {
    if (this.isLoginMode) {
      this.authForm.get('prenom')?.clearValidators();
      this.authForm.get('nom')?.clearValidators();
      this.authForm.get('telephone')?.clearValidators();
      this.authForm.get('motDePasse')?.setValidators([Validators.required]);
    } else {
      this.authForm.get('prenom')?.setValidators([Validators.required]);
      this.authForm.get('nom')?.setValidators([Validators.required]);
      this.authForm.get('telephone')?.setValidators([
        Validators.required,
        Validators.pattern(/^[+]?[0-9]{8,15}$/)
      ]);
      this.authForm.get('motDePasse')?.setValidators([
        Validators.required,
        Validators.minLength(8)
      ]);
    }
    ['prenom', 'nom', 'telephone', 'motDePasse'].forEach(f =>
      this.authForm.get(f)?.updateValueAndValidity()
    );
  }
 
  fieldInvalid(field: string): boolean {
    const ctrl = this.authForm.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
 
  onSubmit() {
    this.error = '';
    this.updateValidators();
 
    if (this.authForm.invalid) {
      this.authForm.markAllAsTouched();
      return;
    }
 
    this.loading = true;
    const v = this.authForm.value;
 
    if (this.isLoginMode) {
      this.authService.login({ email: v.email, motDePasse: v.motDePasse }).subscribe({
        next: () => {
          const role = this.authService.getUserRole();
          this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
        },
        error: () => { this.error = 'Invalid credentials. Please try again.'; this.loading = false; }
      });
    } else {
      this.authService.register({
        email: v.email,
        motDePasse: v.motDePasse,
        prenom: v.prenom,
        nom: v.nom,
        telephone: v.telephone,
      }).subscribe({
        next: () => this.router.navigate(['/home-view']),
        error: (err) => {
          this.error = err?.error?.message || 'Registration failed. Please try again.';
          this.loading = false;
        }
      });
    }
  }
}