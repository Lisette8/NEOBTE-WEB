import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../../Services/auth-service';
import { LoginRequest } from '../../../Entities/Interfaces/login-request';
import { RegisterRequest } from '../../../Entities/Interfaces/register-request';
import { ActivatedRoute, Router } from '@angular/router';
import { ReferralService } from '../../../Services/SharedServices/Referral.service';



@Component({
  selector: 'app-auth-view',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './auth-view.html',
  styleUrl: './auth-view.css',
})
export class AuthView implements OnInit {

  step: 'login' | 'register' | 'pin' | 'pin-forgot' | 'pin-forgot-code'
    | 'forgot' | 'verify-code' | 'new-password' | 'done' = 'login';

  showPassword = false;
  error = '';
  loading = false;
  prefillReferralCode = '';

  // Forms
  authForm: FormGroup;
  forgotForm: FormGroup;
  codeForm: FormGroup;
  newPasswordForm: FormGroup;
  pinForm: FormGroup;
  pinForgotCodeForm: FormGroup;

  // State
  resetEmail = '';
  resetToken = '';
  pinTempToken = '';       // held between login → pin step
  pinBypassSent = false;

  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private referralService: ReferralService
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

    this.pinForm = this.fb.group({
      pin: ['', [Validators.required, Validators.pattern(/^\d{4,6}$/)]],
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
      // ?mode=register → open register tab directly (from "Ouvrir un compte" landing CTA)
      if (params['mode'] === 'register') {
        this.step = 'register';
        this.updateAuthValidators();
      }
      // ?ref=CODE → referral link, switch to register and pre-fill code
      if (params['ref']) {
        this.prefillReferralCode = params['ref'];
        this.step = 'register';
        this.updateAuthValidators();
        this.authForm.patchValue({ referralCode: this.prefillReferralCode });
      }
    });
  }

  // ── Auth ──────────────────────────────────────────────────────────────────

  get isLoginMode() { return this.step === 'login'; }

  toggleMode() {
    this.step = this.step === 'login' ? 'register' : 'login';
    this.error = '';
    this.authForm.reset();
    if (this.prefillReferralCode) this.authForm.patchValue({ referralCode: this.prefillReferralCode });
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
        next: (res) => {
          this.loading = false;
          if (res.pinRequired) {
            // Backend wants PIN verification — store temp token and go to pin step
            this.pinTempToken = res.pinTempToken;
            this.pinForm.reset();
            this.step = 'pin';
          } else {
            const role = this.authService.getUserRole();
            this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
          }
        },
        error: () => { this.error = 'Identifiants invalides. Veuillez réessayer.'; this.loading = false; }
      });
    } else {
      const req: RegisterRequest = {
        email: v.email,
        motDePasse: v.motDePasse,
        prenom: v.prenom,
        nom: v.nom,
        telephone: v.telephone,
      };
      const trimmedCode = v.referralCode?.trim();

      this.authService.register(req).subscribe({
        next: () => {
          if (trimmedCode) {
            this.referralService.applyCode(trimmedCode).subscribe({
              next: () => this.router.navigate(['/home-view']),
              error: (err) => {
                this.error = err?.error?.message || 'Code de parrainage invalide. Votre compte a bien été créé.';
                this.loading = false;
                setTimeout(() => this.router.navigate(['/home-view']), 3000);
              }
            });
          } else {
            this.router.navigate(['/home-view']);
          }
        },
        error: (err) => { this.error = err?.error?.message || 'Inscription échouée.'; this.loading = false; }
      });
    }
  }

  // ── PIN step ──────────────────────────────────────────────────────────────

  submitPin() {
    if (this.pinForm.invalid) { this.pinForm.markAllAsTouched(); return; }
    this.error = '';
    this.loading = true;
    this.authService.verifyPin(this.pinTempToken, this.pinForm.value.pin).subscribe({
      next: () => {
        this.loading = false;
        const role = this.authService.getUserRole();
        this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
      },
      error: (err) => { this.error = err?.error?.message || 'PIN incorrect.'; this.loading = false; }
    });
  }

  goToPinForgot() {
    this.error = '';
    this.pinBypassSent = false;
    this.step = 'pin-forgot';
  }

  sendPinBypassCode() {
    this.error = '';
    this.loading = true;
    this.authService.sendForgotPinCode(this.pinTempToken).subscribe({
      next: () => {
        this.loading = false;
        this.pinBypassSent = true;
        this.pinForgotCodeForm.reset();
        this.step = 'pin-forgot-code';
      },
      error: (err) => { this.error = err?.error?.message || 'Impossible d\'envoyer le code.'; this.loading = false; }
    });
  }

  submitPinBypassCode() {
    if (this.pinForgotCodeForm.invalid) { this.pinForgotCodeForm.markAllAsTouched(); return; }
    this.error = '';
    this.loading = true;
    this.authService.verifyForgotPinCode(this.pinTempToken, this.pinForgotCodeForm.value.code).subscribe({
      next: () => {
        this.loading = false;
        const role = this.authService.getUserRole();
        this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
      },
      error: (err) => { this.error = err?.error?.message || 'Code incorrect.'; this.loading = false; }
    });
  }

  backToPin() { this.step = 'pin'; this.error = ''; }
  backToLogin() { this.step = 'login'; this.error = ''; this.pinTempToken = ''; }

  // Strip anything that isn't a digit or leading +
  sanitizePhone(event: Event, form: FormGroup, field: string) {
    const input = event.target as HTMLInputElement;
    let val = input.value.replace(/[^\d+]/g, '');
    // Only allow + at the start
    if (val.indexOf('+') > 0) val = val.replace(/\+/g, '');
    input.value = val;
    form.get(field)?.setValue(val, { emitEvent: false });
  }

  goToForgot() { this.step = 'forgot'; this.error = ''; this.forgotForm.reset(); }

  submitForgot() {
    if (this.forgotForm.invalid) { this.forgotForm.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';
    const email = this.forgotForm.value.email;
    this.authService.forgotPassword(email).subscribe({
      next: () => { this.resetEmail = email; this.loading = false; this.step = 'verify-code'; },
      error: (err) => { this.error = err?.error?.message || 'Une erreur est survenue.'; this.loading = false; }
    });
  }

  submitCode() {
    if (this.codeForm.invalid) { this.codeForm.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';
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
    this.loading = true;
    this.error = '';
    this.codeForm.reset();
    this.authService.forgotPassword(this.resetEmail).subscribe({
      next: () => { this.loading = false; },
      error: () => { this.error = 'Échec du renvoi du code.'; this.loading = false; }
    });
  }
}