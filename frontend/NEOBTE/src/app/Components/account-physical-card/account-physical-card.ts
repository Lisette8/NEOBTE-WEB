import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Compte } from '../../Entities/Interfaces/compte';

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
    switch (this.compte.typeCompte) {
      case 'COURANT': return 'Compte Chèque';
      case 'EPARGNE': return 'Compte Épargne';
      case 'PROFESSIONNEL': return 'Compte Professionnel';
      default: return this.compte.typeCompte;
    }
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

  maskAccountId(): string {
    const id = String(this.compte.idCompte ?? '');
    if (id.length <= 2) return id;
    return `${id.slice(0, 1)}•••• •••• •••• ${id.slice(-1)}`;
  }
}
