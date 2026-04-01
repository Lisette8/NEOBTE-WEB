import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ACCOUNT_TYPE_META, Compte } from '../../Entities/Interfaces/compte';

@Component({
  selector: 'app-account-physical-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './account-physical-card.html',
  styleUrl: './account-physical-card.css',
})
export class AccountPhysicalCard {
  @Input({ required: true }) compte!: Compte;
  @Input() linkEnabled = true;
  @Input() showCta = true;
  /** Provide an `assets/...` path (not `../../..`). Example: `assets/logo-mark.png` */
  @Input() logoSrc = 'assets/logo-mark.png';
  
  private logoErrored = false;
  get resolvedLogoSrc(): string {
    if (this.logoErrored) return 'assets/logo-mark.png';
    return this.normalizeAssetPath(this.logoSrc) || 'assets/logo-mark.png';
  }

  onLogoError() {
    this.logoErrored = true;
  }

  private normalizeAssetPath(src: string | null | undefined): string {
    const s = (src ?? '').trim();
    if (!s) return '';
    // Normalize any user-supplied relative FS-like path (../../../../assets/...) to `assets/...`.
    const idx = s.lastIndexOf('/assets/');
    if (idx >= 0) return s.slice(idx + 1); // "assets/..."
    const j = s.lastIndexOf('assets/');
    if (j >= 0) return s.slice(j);
    return s;
  }

  get label(): string {
    return ACCOUNT_TYPE_META[this.compte.typeCompte]?.label ?? this.compte.typeCompte;
  }

  get themeClass(): string {
    switch (this.compte.typeCompte) {
      case 'COURANT': return 'theme-courant';
      case 'EPARGNE': return 'theme-epargne';
      case 'PROFESSIONNEL': return 'theme-pro';
      default: return 'theme-default';
    }
  }

  get statusLabel(): string {
    switch (this.compte.statutCompte) {
      case 'ACTIVE': return 'Actif';
      case 'SUSPENDU':
      case 'SUSPENDED': return 'Suspendu';
      case 'BLOQUE':
      case 'BLOCKED': return 'Bloqué';
      case 'CLOTURE':
      case 'CLOSED': return 'Clôturé';
      default: return this.compte.statutCompte;
    }
  }

  /** Savings accounts display their annual interest rate on the card */
  get interestRate(): number {
    return ACCOUNT_TYPE_META[this.compte.typeCompte]?.interestRate ?? 0;
  }

  /** Show a small restriction badge on the card for savings */
  get isSavings(): boolean {
    return this.compte.typeCompte === 'EPARGNE';
  }

  get isPro(): boolean {
    return this.compte.typeCompte === 'PROFESSIONNEL';
  }

  maskAccountId(): string {
    const id = String(this.compte.idCompte ?? '');
    if (id.length <= 2) return id;
    return `${id.slice(0, 1)}•••• •••• •••• ${id.slice(-1)}`;
  }
}
