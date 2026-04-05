import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../../Services/auth-service';
import { LoginRequest } from '../../../Entities/Interfaces/login-request';
import { RegisterRequest } from '../../../Entities/Interfaces/register-request';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-auth-view',
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './auth-view.html',
  styleUrl: './auth-view.css',
})
export class AuthView implements OnInit {

  step: 'login' | 'register' | 'forgot' | 'verify-code' | 'new-password' | 'done' | 'pin' | 'pin-forgot' | 'pin-forgot-code' = 'login';
  showPassword = false;
  error = '';
  loading = false;
  pinTempToken = '';
  prefillReferralCode = '';

  authForm: FormGroup;
  forgotForm: FormGroup;
  codeForm: FormGroup;
  newPasswordForm: FormGroup;
  pinForm: FormGroup;
  pinForgotCodeForm: FormGroup;

  resetEmail = '';
  resetToken = '';

  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.authForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', Validators.required],
      prenom: [''],
      nom: [''],
      telephone: [''],
      referralCode: [''],
    });

    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });

    this.codeForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
    });

    this.newPasswordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });

    // PIN forms — declared here so fb is already initialised
    this.pinForm = this.fb.group({
      pin: ['', [Validators.required, Validators.pattern(/^[0-9]{4,6}$/)]],
    });

    this.pinForgotCodeForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
    });
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getUserRole();
      this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
      return;
    }

    this.route.queryParams.subscribe(params => {
      if (params['mode'] === 'register') {
        this.step = 'register';
        this.updateAuthValidators();
      }
      if (params['ref']) {
        this.prefillReferralCode = params['ref'];
        this.step = 'register';
        this.updateAuthValidators();
        this.authForm.patchValue({ referralCode: this.prefillReferralCode });
      }
    });
  }

  // ── Auth ─────────────────────────────────────────────────────────────

  get isLoginMode() { return this.step === 'login'; }

  toggleMode() {
    this.step = this.step === 'login' ? 'register' : 'login';
    this.error = '';
    this.authForm.reset();
    if (this.prefillReferralCode) this.authForm.patchValue({ referralCode: this.prefillReferralCode });
    this.updateAuthValidators();
  }

  switchToLogin() { this.step = 'login'; this.updateAuthValidators(); this.error = ''; }
  switchToRegister() { this.step = 'register'; this.updateAuthValidators(); this.error = ''; }

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

  // ── Password strength ───────────────────────────────────────────────────

  /** 0=empty, 1=weak, 2=fair, 3=strong, 4=very-strong */
  passwordStrength(value: string): number {
    if (!value) return 0;
    let score = 0;
    if (value.length >= 8) score++;
    if (value.length >= 12) score++;
    if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score++;
    if (/[0-9]/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;
    // cap to 4
    return Math.min(4, score);
  }

  get registerPasswordStrength(): number {
    return this.passwordStrength(this.authForm.get('motDePasse')?.value ?? '');
  }

  get newPasswordStrength(): number {
    return this.passwordStrength(this.newPasswordForm?.get('newPassword')?.value ?? '');
  }

  passwordStrengthLabel(score: number): string {
    return ['', 'Trop court', 'Faible', 'Moyen', 'Fort'][score] ?? '';
  }

  passwordStrengthClass(score: number): string {
    return ['', 'pw-weak', 'pw-fair', 'pw-good', 'pw-strong'][score] ?? '';
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
        next: (res) => {
          this.loading = false;
          if (res.pinRequired) {
            this.pinTempToken = res.pinTempToken;
            this.step = 'pin';
            return;
          }
          const role = this.authService.getUserRole();
          this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
        },
        error: (err) => {
          this.error = err?.error?.message || 'Identifiants invalides. Veuillez réessayer.';
          this.loading = false;
        }
      });
    } else {
      const req: RegisterRequest = {
        email: v.email,
        motDePasse: v.motDePasse,
        prenom: v.prenom,
        nom: v.nom,
        telephone: v.telephone,
        referralCode: v.referralCode?.trim() || undefined,
      };
      this.authService.register(req).subscribe({
        next: () => this.router.navigate(['/home-view']),
        error: (err) => { this.error = err?.error?.message || 'Inscription échouée.'; this.loading = false; }
      });
    }
  }

  // ── Forgot password ───────────────────────────────────────────────────

  goToForgot() { this.step = 'forgot'; this.error = ''; this.forgotForm.reset(); }
  backToLogin() { this.step = 'login'; this.error = ''; }

  submitForgot() {
    if (this.forgotForm.invalid) { this.forgotForm.markAllAsTouched(); return; }
    this.loading = true; this.error = '';
    const email = this.forgotForm.value.email;
    this.authService.forgotPassword(email).subscribe({
      next: () => { this.resetEmail = email; this.loading = false; this.step = 'verify-code'; },
      error: (err) => { this.error = err?.error?.message || 'Une erreur est survenue.'; this.loading = false; }
    });
  }

  submitCode() {
    if (this.codeForm.invalid) { this.codeForm.markAllAsTouched(); return; }
    this.loading = true; this.error = '';
    this.authService.verifyResetCode(this.resetEmail, this.codeForm.value.code).subscribe({
      next: (res) => { this.resetToken = res.resetToken; this.loading = false; this.step = 'new-password'; },
      error: (err) => { this.error = err?.error?.message || 'Code invalide.'; this.loading = false; }
    });
  }

  submitNewPassword() {
    this.error = '';
    const v = this.newPasswordForm.value;
    if (this.newPasswordForm.invalid) { this.newPasswordForm.markAllAsTouched(); return; }
    if (v.newPassword !== v.confirmPassword) { this.error = 'Les mots de passe ne correspondent pas.'; return; }
    this.loading = true;
    this.authService.resetPassword(this.resetToken, v.newPassword).subscribe({
      next: () => { this.loading = false; this.step = 'done'; },
      error: (err) => { this.error = err?.error?.message || 'Réinitialisation échouée.'; this.loading = false; }
    });
  }

  resendCode() {
    this.loading = true; this.error = '';
    this.codeForm.reset();
    this.authService.forgotPassword(this.resetEmail).subscribe({
      next: () => { this.loading = false; },
      error: () => { this.error = 'Échec du renvoi du code.'; this.loading = false; }
    });
  }

  goToForgot_v2() { this.step = 'forgot'; this.error = ''; }

  // ── PIN 2FA ───────────────────────────────────────────────────────────

  submitPin() {
    if (this.pinForm.invalid) { this.pinForm.markAllAsTouched(); return; }
    this.loading = true; this.error = '';
    const pin = this.pinForm.value.pin as string;
    this.authService.verifyPin(this.pinTempToken, pin).subscribe({
      next: () => {
        this.loading = false;
        const role = this.authService.getUserRole();
        this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
      },
      error: (err) => { this.error = err?.error?.message || 'PIN incorrect.'; this.loading = false; }
    });
  }

  goToPinForgot() { this.step = 'pin-forgot'; this.error = ''; }
  backToPin() { this.step = 'pin'; this.error = ''; }

  sendPinBypassCode() {
    this.loading = true; this.error = '';
    this.authService.sendForgotPinCode(this.pinTempToken).subscribe({
      next: () => { this.loading = false; this.step = 'pin-forgot-code'; },
      error: (err) => { this.error = err?.error?.message || 'Envoi échoué.'; this.loading = false; }
    });
  }

  submitPinBypassCode() {
    if (this.pinForgotCodeForm.invalid) { this.pinForgotCodeForm.markAllAsTouched(); return; }
    this.loading = true; this.error = '';
    const code = this.pinForgotCodeForm.value.code as string;
    this.authService.verifyForgotPinCode(this.pinTempToken, code).subscribe({
      next: () => {
        this.loading = false;
        const role = this.authService.getUserRole();
        this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
      },
      error: (err) => { this.error = err?.error?.message || 'Code invalide.'; this.loading = false; }
    });
  }
}