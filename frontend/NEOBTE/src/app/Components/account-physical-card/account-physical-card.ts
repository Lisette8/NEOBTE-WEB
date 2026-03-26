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
