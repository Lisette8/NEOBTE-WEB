import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../Services/auth-service';
import { ChangePasswordRequest, ClientProfile, UpdateClientProfileRequest } from '../../../Entities/Interfaces/client-profile';

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

  section: 'profil' | 'securite' | 'abonnement' | 'notifications' | 'assistance' | 'bte' | 'a-propos' = 'profil';

  // Profile form
  form: UpdateClientProfileRequest = {
    nom: '',
    prenom: '',
    telephone: '',
    job: '',
    genre: null,
    adresse: '',
    codePostal: '',
    pays: 'Tunisie',
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

  // Forgot password (same flow as auth page)
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

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  setSection(section: SettingsView['section']) {
    this.section = section;
    this.profileSuccess = '';
    this.profileError = '';
    this.passwordSuccess = '';
    this.passwordError = '';
    this.photoError = '';
    this.fpError = '';
    this.fpSuccess = '';
  }

  loadProfile() {
    this.loading = true;
    this.error = '';
    this.authService.getCurrentUser().subscribe({
      next: (p) => {
        this.profile = p;
        this.form = {
          nom: p.nom ?? '',
          prenom: p.prenom ?? '',
          telephone: p.telephone ?? '',
          job: p.job ?? '',
          genre: (p.genre as any) ?? null,
          adresse: p.adresse ?? '',
          codePostal: p.codePostal ?? '',
          pays: p.pays ?? 'Tunisie',
        };
        this.photoPreviewUrl = p.photoUrl ? this.mediaUrl(p.photoUrl) : null;
        this.fpEmail = p.email ?? '';
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger votre profil.';
        this.loading = false;
      },
    });
  }

  logout() {
    this.logoutError = '';
    this.loggingOut = true;
    this.authService.logout().subscribe({
      next: () => {
        this.loggingOut = false;
        this.router.navigate(['/auth-view']);
      },
      error: () => {
        this.loggingOut = false;
        this.logoutError = 'Impossible de se déconnecter. Veuillez réessayer.';
      },
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
        this.profile = p;
        this.photoFile = null;
        this.photoPreviewUrl = p.photoUrl ? this.mediaUrl(p.photoUrl) : null;
        this.uploadingPhoto = false;
      },
      error: (err) => {
        this.photoError = this.extractErrorMessage(err) || "Impossible d'enregistrer la photo.";
        this.uploadingPhoto = false;
      },
    });
  }

  saveProfile() {
    this.savingProfile = true;
    this.profileSuccess = '';
    this.profileError = '';
    this.authService.updateProfile(this.form).subscribe({
      next: (p) => {
        this.profile = p;
        this.profileSuccess = 'Profil mis à jour avec succès.';
        this.savingProfile = false;
        setTimeout(() => (this.profileSuccess = ''), 3000);
      },
      error: (err) => {
        this.profileError = this.extractErrorMessage(err) || 'Erreur lors de la mise à jour du profil.';
        this.savingProfile = false;
      },
    });
  }

  savePassword() {
    this.passwordSuccess = '';
    this.passwordError = '';
    this.passwordFieldError = { old: '', n1: '', n2: '' };

    if (!this.oldPassword || !this.newPassword) {
      if (!this.oldPassword) this.passwordFieldError.old = "Veuillez saisir votre ancien mot de passe.";
      if (!this.newPassword) this.passwordFieldError.n1 = "Veuillez saisir un nouveau mot de passe.";
      if (!this.confirmPassword) this.passwordFieldError.n2 = "Veuillez confirmer le nouveau mot de passe.";
      this.passwordError = 'Veuillez corriger les champs en rouge.';
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.passwordFieldError.n2 = 'La confirmation ne correspond pas.';
      this.passwordError = 'La confirmation ne correspond pas.';
      return;
    }
    if (this.newPassword.length < 6) {
      this.passwordFieldError.n1 = 'Le mot de passe doit contenir au moins 6 caractères.';
      this.passwordError = 'Le mot de passe doit contenir au moins 6 caractères.';
      return;
    }

    const dto: ChangePasswordRequest = { oldPassword: this.oldPassword, newPassword: this.newPassword };
    this.savingPassword = true;
    this.authService.changePassword(dto).subscribe({
      next: () => {
        this.passwordSuccess = 'Mot de passe mis à jour.';
        this.savingPassword = false;
        this.oldPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        setTimeout(() => (this.passwordSuccess = ''), 3000);
      },
      error: (err) => {
        this.passwordError = this.extractErrorMessage(err) || 'Erreur lors du changement de mot de passe.';
        this.savingPassword = false;
      },
    });
  }

  // ── Forgot password flow ────────────────────────────────────────────────

  toggleForgotPassword() {
    this.showForgotPassword = !this.showForgotPassword;
    this.fpError = '';
    this.fpSuccess = '';
    this.fpStep = 'email';
    this.fpCode = '';
    this.fpResetToken = '';
    this.fpNewPassword = '';
    this.fpConfirmPassword = '';
  }

  sendResetCode() {
    this.fpError = '';
    this.fpSuccess = '';
    if (!this.fpEmail || !this.fpEmail.includes('@')) {
      this.fpError = "Veuillez saisir une adresse e-mail valide.";
      return;
    }
    this.fpLoading = true;
    this.authService.forgotPassword(this.fpEmail).subscribe({
      next: () => {
        this.fpLoading = false;
        this.fpStep = 'code';
        this.fpSuccess = "Un code a été envoyé par e-mail (si un compte existe).";
        setTimeout(() => (this.fpSuccess = ''), 3500);
      },
      error: (err) => {
        this.fpLoading = false;
        this.fpError = this.extractErrorMessage(err) || "Impossible d'envoyer le code.";
      }
    });
  }

  verifyResetCode() {
    this.fpError = '';
    this.fpSuccess = '';
    const code = (this.fpCode ?? '').trim();
    if (!/^[0-9]{6}$/.test(code)) {
      this.fpError = "Le code doit contenir 6 chiffres.";
      return;
    }
    this.fpLoading = true;
    this.authService.verifyResetCode(this.fpEmail, code).subscribe({
      next: (res) => {
        this.fpResetToken = res.resetToken;
        this.fpLoading = false;
        this.fpStep = 'new';
      },
      error: (err) => {
        this.fpLoading = false;
        this.fpError = this.extractErrorMessage(err) || "Code invalide.";
      }
    });
  }

  resetPassword() {
    this.fpError = '';
    this.fpSuccess = '';
    if (!this.fpNewPassword || this.fpNewPassword.length < 8) {
      this.fpError = "Le mot de passe doit contenir au moins 8 caractères.";
      return;
    }
    if (this.fpNewPassword !== this.fpConfirmPassword) {
      this.fpError = "La confirmation ne correspond pas.";
      return;
    }
    if (!this.fpResetToken) {
      this.fpError = "Session expirée. Veuillez recommencer.";
      this.fpStep = 'email';
      return;
    }
    this.fpLoading = true;
    this.authService.resetPassword(this.fpResetToken, this.fpNewPassword).subscribe({
      next: () => {
        this.fpLoading = false;
        this.fpStep = 'done';
      },
      error: (err) => {
        this.fpLoading = false;
        this.fpError = this.extractErrorMessage(err) || "Impossible de réinitialiser le mot de passe.";
      }
    });
  }

  resendResetCode() {
    this.fpCode = '';
    this.sendResetCode();
  }

  private extractErrorMessage(err: any): string {
    const e = err?.error;
    if (!e) return '';
    if (typeof e === 'string') return e;
    if (e.message || e.detail) return e.message || e.detail;
    if (Array.isArray(e.errors) && e.errors.length > 0) return String(e.errors[0]);
    if (e.errors && typeof e.errors === 'object') {
      const first = Object.values(e.errors)[0];
      if (first) return String(first);
    }
    return '';
  }
}
