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
 
  step: 'login' | 'register' | 'forgot' | 'verify-code' | 'new-password' | 'done' = 'login';
  showPassword = false;
  error = '';
  loading = false;
 
  // Forms
  authForm: FormGroup;
  forgotForm: FormGroup;
  codeForm: FormGroup;
  newPasswordForm: FormGroup;
 
  // State passed between steps
  resetEmail = '';
  resetToken = '';
 
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
 
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
 
    this.codeForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
    });
 
    this.newPasswordForm = this.fb.group({
      newPassword:     ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });
  }
 
  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getUserRole();
      this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
    }
  }
 
  // ── Auth (login / register) ──────────────────────────────────────────
 
  get isLoginMode() { return this.step === 'login'; }
 
  toggleMode() {
    this.step = this.step === 'login' ? 'register' : 'login';
    this.error = '';
    this.authForm.reset();
    this.updateAuthValidators();
  }
 
  private updateAuthValidators() {
    if (this.step === 'login') {
      ['prenom', 'nom', 'telephone'].forEach(f => this.authForm.get(f)?.clearValidators());
      this.authForm.get('motDePasse')?.setValidators([Validators.required]);
    } else {
      this.authForm.get('prenom')?.setValidators([Validators.required]);
      this.authForm.get('nom')?.setValidators([Validators.required]);
      this.authForm.get('telephone')?.setValidators([Validators.required, Validators.pattern(/^[+]?[0-9]{8,15}$/)]);
      this.authForm.get('motDePasse')?.setValidators([Validators.required, Validators.minLength(8)]);
    }
    ['prenom', 'nom', 'telephone', 'motDePasse'].forEach(f => this.authForm.get(f)?.updateValueAndValidity());
  }
 
  fieldInvalid(form: FormGroup, field: string): boolean {
    const ctrl = form.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
 
  onSubmitAuth() {
    this.error = '';
    this.updateAuthValidators();
    if (this.authForm.invalid) { this.authForm.markAllAsTouched(); return; }
 
    this.loading = true;
    const v = this.authForm.value;
 
    if (this.step === 'login') {
      this.authService.login({ email: v.email, motDePasse: v.motDePasse }).subscribe({
        next: () => {
          const role = this.authService.getUserRole();
          this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
        },
        error: () => { this.error = 'Invalid credentials. Please try again.'; this.loading = false; }
      });
    } else {
      this.authService.register({ email: v.email, motDePasse: v.motDePasse, prenom: v.prenom, nom: v.nom, telephone: v.telephone }).subscribe({
        next: () => this.router.navigate(['/home-view']),
        error: (err) => { this.error = err?.error?.message || 'Registration failed.'; this.loading = false; }
      });
    }
  }
 
  // ── Forgot Password flow ─────────────────────────────────────────────
 
  goToForgot() { this.step = 'forgot'; this.error = ''; this.forgotForm.reset(); }
  backToLogin() { this.step = 'login'; this.error = ''; }
 
  submitForgot() {
    if (this.forgotForm.invalid) { this.forgotForm.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';
    const email = this.forgotForm.value.email;
 
    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.resetEmail = email;
        this.loading = false;
        this.step = 'verify-code';
      },
      error: (err) => { this.error = err?.error?.message || 'Something went wrong.'; this.loading = false; }
    });
  }
 
  submitCode() {
    if (this.codeForm.invalid) { this.codeForm.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';
 
    this.authService.verifyResetCode(this.resetEmail, this.codeForm.value.code).subscribe({
      next: (res) => {
        this.resetToken = res.resetToken;
        this.loading = false;
        this.step = 'new-password';
      },
      error: (err) => { this.error = err?.error?.message || 'Invalid code.'; this.loading = false; }
    });
  }
 
  submitNewPassword() {
    this.error = '';
    const v = this.newPasswordForm.value;
    if (this.newPasswordForm.invalid) { this.newPasswordForm.markAllAsTouched(); return; }
    if (v.newPassword !== v.confirmPassword) { this.error = 'Passwords do not match.'; return; }
 
    this.loading = true;
    this.authService.resetPassword(this.resetToken, v.newPassword).subscribe({
      next: () => { this.loading = false; this.step = 'done'; },
      error: (err) => { this.error = err?.error?.message || 'Reset failed.'; this.loading = false; }
    });
  }
 
  resendCode() {
    this.loading = true;
    this.error = '';
    this.codeForm.reset();
    this.authService.forgotPassword(this.resetEmail).subscribe({
      next: () => { this.loading = false; },
      error: () => { this.error = 'Failed to resend code.'; this.loading = false; }
    });
  }
}