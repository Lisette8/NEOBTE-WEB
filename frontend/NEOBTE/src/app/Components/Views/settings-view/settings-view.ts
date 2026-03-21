import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../Services/auth-service';
import { ChangePasswordRequest, ClientProfile, UpdateClientProfileRequest } from '../../../Entities/Interfaces/client-profile';
import { ReferralDashboard, ReferralService } from '../../../Services/SharedServices/Referral.service';

@Component({
  selector: 'app-settings-view',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './settings-view.html',
  styleUrl: './settings-view.css',
})
export class SettingsView implements OnInit {
  profile: ClientProfile | null = null;
  loading = true;
  error = '';

  section: 'profil' | 'securite' | 'abonnement' | 'parrainage' | 'notifications' | 'assistance' | 'bte' | 'a-propos' = 'profil';

  // Profile form
  form: UpdateClientProfileRequest = {
    nom: '', prenom: '', telephone: '', job: '',
    genre: null, adresse: '', codePostal: '', pays: 'Tunisie',
  };

  savingProfile = false;
  profileSuccess = '';
  profileError = '';

  // Photo
  photoPreviewUrl: string | null = null;
  photoFile: File | null = null;
  uploadingPhoto = false;
  photoError = '';

  // Password
  oldPassword = '';
  newPassword = '';
  confirmPassword = '';
  savingPassword = false;
  passwordSuccess = '';
  passwordError = '';
  passwordFieldError = { old: '', n1: '', n2: '' };

  // Forgot password
  showForgotPassword = false;
  fpStep: 'email' | 'code' | 'new' | 'done' = 'email';
  fpEmail = '';
  fpCode = '';
  fpResetToken = '';
  fpNewPassword = '';
  fpConfirmPassword = '';
  fpLoading = false;
  fpError = '';
  fpSuccess = '';

  loggingOut = false;
  logoutError = '';

  // Referral
  referral: ReferralDashboard | null = null;
  referralLoading = false;
  codeCopied = false;
  linkCopied = false;

  // PIN 2FA
  pinSection: 'idle' | 'enable' | 'change' | 'disable' | 'forgot-send' | 'forgot-code' = 'idle';
  pinInput = '';
  pinConfirm = '';
  pinOld = '';
  pinResetCode = '';
  pinLoading = false;
  pinSuccess = '';
  pinError = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private referralService: ReferralService
  ) { }

  ngOnInit(): void {
    this.loadProfile();
  }

  setSection(section: SettingsView['section']) {
    this.section = section;
    this.profileSuccess = ''; this.profileError = '';
    this.passwordSuccess = ''; this.passwordError = '';
    this.photoError = ''; this.fpError = ''; this.fpSuccess = '';
    if (section === 'parrainage' && !this.referral) this.loadReferral();
  }

  loadProfile() {
    this.loading = true;
    this.error = '';
    this.authService.getCurrentUser().subscribe({
      next: (p) => {
        this.profile = p;
        this.form = {
          nom: p.nom ?? '', prenom: p.prenom ?? '', telephone: p.telephone ?? '',
          job: p.job ?? '', genre: (p.genre as any) ?? null,
          adresse: p.adresse ?? '', codePostal: p.codePostal ?? '', pays: p.pays ?? 'Tunisie',
        };
        this.photoPreviewUrl = p.photoUrl ? this.mediaUrl(p.photoUrl) : null;
        this.fpEmail = p.email ?? '';
        this.loading = false;
      },
      error: () => { this.error = 'Impossible de charger votre profil.'; this.loading = false; },
    });
  }

  // ── Referral ─────────────────────────────────────────────────────────────

  loadReferral() {
    this.referralLoading = true;
    this.referralService.getDashboard().subscribe({
      next: (r) => { this.referral = r; this.referralLoading = false; },
      error: () => { this.referralLoading = false; }
    });
  }

  copyCode() {
    if (!this.referral?.referralCode) return;
    navigator.clipboard.writeText(this.referral.referralCode).then(() => {
      this.codeCopied = true;
      setTimeout(() => this.codeCopied = false, 2000);
    });
  }

  copyLink() {
    if (!this.referral?.referralLink) return;
    navigator.clipboard.writeText(this.referral.referralLink).then(() => {
      this.linkCopied = true;
      setTimeout(() => this.linkCopied = false, 2000);
    });
  }

  daysLeft(expiresAt: string | null): number {
    if (!expiresAt) return 0;
    const diff = new Date(expiresAt).getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  // ── Profile ───────────────────────────────────────────────────────────────

  logout() {
    this.logoutError = '';
    this.loggingOut = true;
    this.authService.logout().subscribe({
      next: () => { this.loggingOut = false; this.router.navigate(['/auth-view']); },
      error: () => { this.loggingOut = false; this.logoutError = 'Impossible de se déconnecter.'; },
    });
  }

  mediaUrl(url?: string | null): string {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`;
  }

  onSelectPhoto(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.photoFile = file;
    this.photoError = '';
    this.photoPreviewUrl = file ? URL.createObjectURL(file) : (this.profile?.photoUrl ? this.mediaUrl(this.profile.photoUrl) : null);
  }

  uploadPhoto() {
    if (!this.photoFile) return;
    this.uploadingPhoto = true;
    this.photoError = '';
    this.authService.uploadProfilePhoto(this.photoFile).subscribe({
      next: (p) => {
        this.profile = p; this.photoFile = null;
        this.photoPreviewUrl = p.photoUrl ? this.mediaUrl(p.photoUrl) : null;
        this.uploadingPhoto = false;
      },
      error: (err) => { this.photoError = this.extractErrorMessage(err) || "Impossible d'enregistrer la photo."; this.uploadingPhoto = false; },
    });
  }

  saveProfile() {
    this.savingProfile = true; this.profileSuccess = ''; this.profileError = '';
    this.authService.updateProfile(this.form).subscribe({
      next: (p) => {
        this.profile = p; this.profileSuccess = 'Profil mis à jour avec succès.';
        this.savingProfile = false;
        setTimeout(() => this.profileSuccess = '', 3000);
      },
      error: (err) => { this.profileError = this.extractErrorMessage(err) || 'Erreur de mise à jour.'; this.savingProfile = false; },
    });
  }

  savePassword() {
    this.passwordSuccess = ''; this.passwordError = '';
    this.passwordFieldError = { old: '', n1: '', n2: '' };
    if (!this.oldPassword) this.passwordFieldError.old = 'Requis.';
    if (!this.newPassword) this.passwordFieldError.n1 = 'Requis.';
    if (!this.confirmPassword) this.passwordFieldError.n2 = 'Requis.';
    if (!this.oldPassword || !this.newPassword || !this.confirmPassword) { this.passwordError = 'Veuillez remplir tous les champs.'; return; }
    if (this.newPassword !== this.confirmPassword) { this.passwordFieldError.n2 = 'Ne correspond pas.'; this.passwordError = 'Les mots de passe ne correspondent pas.'; return; }
    if (this.newPassword.length < 6) { this.passwordFieldError.n1 = 'Minimum 6 caractères.'; this.passwordError = 'Minimum 6 caractères.'; return; }

    const dto: ChangePasswordRequest = { oldPassword: this.oldPassword, newPassword: this.newPassword };
    this.savingPassword = true;
    this.authService.changePassword(dto).subscribe({
      next: () => {
        this.passwordSuccess = 'Mot de passe mis à jour.'; this.savingPassword = false;
        this.oldPassword = ''; this.newPassword = ''; this.confirmPassword = '';
        setTimeout(() => this.passwordSuccess = '', 3000);
      },
      error: (err) => { this.passwordError = this.extractErrorMessage(err) || 'Erreur.'; this.savingPassword = false; },
    });
  }

  // ── Forgot password ───────────────────────────────────────────────────────

  toggleForgotPassword() {
    this.showForgotPassword = !this.showForgotPassword;
    this.fpError = ''; this.fpSuccess = ''; this.fpStep = 'email';
    this.fpCode = ''; this.fpResetToken = ''; this.fpNewPassword = ''; this.fpConfirmPassword = '';
  }

  sendResetCode() {
    this.fpError = ''; this.fpSuccess = '';
    if (!this.fpEmail || !this.fpEmail.includes('@')) { this.fpError = 'Adresse e-mail invalide.'; return; }
    this.fpLoading = true;
    this.authService.forgotPassword(this.fpEmail).subscribe({
      next: () => { this.fpLoading = false; this.fpStep = 'code'; this.fpSuccess = 'Code envoyé par e-mail.'; setTimeout(() => this.fpSuccess = '', 3500); },
      error: (err) => { this.fpLoading = false; this.fpError = this.extractErrorMessage(err) || "Échec d'envoi."; }
    });
  }

  verifyResetCode() {
    this.fpError = ''; this.fpSuccess = '';
    const code = (this.fpCode ?? '').trim();
    if (!/^[0-9]{6}$/.test(code)) { this.fpError = 'Code à 6 chiffres requis.'; return; }
    this.fpLoading = true;
    this.authService.verifyResetCode(this.fpEmail, code).subscribe({
      next: (res) => { this.fpResetToken = res.resetToken; this.fpLoading = false; this.fpStep = 'new'; },
      error: (err) => { this.fpLoading = false; this.fpError = this.extractErrorMessage(err) || 'Code invalide.'; }
    });
  }

  resetPassword() {
    this.fpError = ''; this.fpSuccess = '';
    if (!this.fpNewPassword || this.fpNewPassword.length < 8) { this.fpError = 'Minimum 8 caractères.'; return; }
    if (this.fpNewPassword !== this.fpConfirmPassword) { this.fpError = 'Ne correspond pas.'; return; }
    this.fpLoading = true;
    this.authService.resetPassword(this.fpResetToken, this.fpNewPassword).subscribe({
      next: () => { this.fpLoading = false; this.fpStep = 'done'; },
      error: (err) => { this.fpLoading = false; this.fpError = this.extractErrorMessage(err) || 'Échec.'; }
    });
  }

  resendResetCode() { this.fpCode = ''; this.sendResetCode(); }

  // ── PIN 2FA ───────────────────────────────────────────────────────────────

  private resetPinState() {
    this.pinInput = ''; this.pinConfirm = ''; this.pinOld = ''; this.pinResetCode = '';
    this.pinSuccess = ''; this.pinError = ''; this.pinLoading = false;
  }

  enablePin() {
    this.pinError = '';
    if (!this.pinInput || !/^\d{4,6}$/.test(this.pinInput)) {
      this.pinError = 'Le PIN doit contenir 4 à 6 chiffres.'; return;
    }
    if (this.pinInput !== this.pinConfirm) {
      this.pinError = 'Les PINs ne correspondent pas.'; return;
    }
    this.pinLoading = true;
    this.authService.enablePin(this.pinInput).subscribe({
      next: () => {
        this.pinSuccess = 'PIN activé avec succès.';
        this.pinSection = 'idle';
        this.resetPinState();
        this.loadProfile(); // refresh pinEnabled on profile
        setTimeout(() => this.pinSuccess = '', 3000);
      },
      error: (err) => { this.pinError = this.extractErrorMessage(err) || 'Erreur.'; this.pinLoading = false; }
    });
  }

  disablePin() {
    this.pinError = '';
    if (!this.pinOld) { this.pinError = 'Saisissez votre PIN actuel.'; return; }
    this.pinLoading = true;
    this.authService.disablePin(this.pinOld).subscribe({
      next: () => {
        this.pinSuccess = 'PIN désactivé.';
        this.pinSection = 'idle';
        this.resetPinState();
        this.loadProfile();
        setTimeout(() => this.pinSuccess = '', 3000);
      },
      error: (err) => { this.pinError = this.extractErrorMessage(err) || 'PIN incorrect.'; this.pinLoading = false; }
    });
  }

  changePin() {
    this.pinError = '';
    if (!this.pinOld) { this.pinError = 'Saisissez votre ancien PIN.'; return; }
    if (!this.pinInput || !/^\d{4,6}$/.test(this.pinInput)) {
      this.pinError = 'Le nouveau PIN doit contenir 4 à 6 chiffres.'; return;
    }
    if (this.pinInput !== this.pinConfirm) {
      this.pinError = 'Les PINs ne correspondent pas.'; return;
    }
    this.pinLoading = true;
    this.authService.changePin(this.pinOld, this.pinInput).subscribe({
      next: () => {
        this.pinSuccess = 'PIN modifié avec succès.';
        this.pinSection = 'idle';
        this.resetPinState();
        setTimeout(() => this.pinSuccess = '', 3000);
      },
      error: (err) => { this.pinError = this.extractErrorMessage(err) || 'Erreur.'; this.pinLoading = false; }
    });
  }

  sendPinResetCode() {
    this.pinError = '';
    this.pinLoading = true;
    this.authService.sendPinResetFromSettings().subscribe({
      next: () => {
        this.pinLoading = false;
        this.pinSection = 'forgot-code';
        this.pinResetCode = '';
      },
      error: (err) => { this.pinError = this.extractErrorMessage(err) || 'Erreur d\'envoi.'; this.pinLoading = false; }
    });
  }

  verifyPinResetCode() {
    this.pinError = '';
    if (!this.pinResetCode || !/^\d{6}$/.test(this.pinResetCode)) {
      this.pinError = 'Entrez le code à 6 chiffres reçu par e-mail.'; return;
    }
    this.pinLoading = true;
    this.authService.verifyPinResetFromSettings(this.pinResetCode).subscribe({
      next: () => {
        this.pinSuccess = 'PIN désactivé. Vous pouvez maintenant en définir un nouveau.';
        this.pinSection = 'idle';
        this.resetPinState();
        this.loadProfile();
        setTimeout(() => this.pinSuccess = '', 4000);
      },
      error: (err) => { this.pinError = this.extractErrorMessage(err) || 'Code incorrect.'; this.pinLoading = false; }
    });
  }

  private extractErrorMessage(err: any): string {
    const e = err?.error;
    if (!e) return '';
    if (typeof e === 'string') return e;
    if (e.message || e.detail) return e.message || e.detail;
    if (Array.isArray(e.errors) && e.errors.length > 0) return String(e.errors[0]);
    if (e.errors && typeof e.errors === 'object') { const first = Object.values(e.errors)[0]; if (first) return String(first); }
    return '';
  }
}