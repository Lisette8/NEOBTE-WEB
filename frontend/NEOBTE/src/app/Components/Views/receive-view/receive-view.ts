import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Rib, RibCompte } from '../../../Entities/Interfaces/rib';
import { AuthService } from '../../../Services/auth-service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-receive-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './receive-view.html',
  styleUrl: './receive-view.css',
})
export class ReceiveView implements OnInit {
 
  rib: Rib | null = null;
  loading = true;
  error = '';
  copiedField = '';
 
  // The account the user tapped from — shown first, not necessarily the primary
  selectedCompteId: number | null = null;
 
  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}
 
  ngOnInit() {
    // Read compteId from query params if coming from account-detail
    this.route.queryParams.subscribe(params => {
      if (params['compteId']) {
        this.selectedCompteId = Number(params['compteId']);
      }
    });
 
    this.authService.getMyRib().subscribe({
      next: (data) => { this.rib = data; this.loading = false; },
      error: () => { this.error = 'Impossible de charger les détails du compte.'; this.loading = false; }
    });
  }
 
  // The account to show prominently — the one the user came from, or primary as fallback
  get featuredAccount(): RibCompte | null {
    if (!this.rib) return null;
    if (this.selectedCompteId) {
      const selected = this.rib.comptes.find(c => c.idCompte === this.selectedCompteId);
      if (selected) return selected;
    }
    // fallback to primary
    return this.rib.comptes.find(c => c.isPrimary) ?? this.rib.comptes[0] ?? null;
  }
 
  // All other accounts (not the featured one)
  get otherAccounts(): RibCompte[] {
    if (!this.rib) return [];
    return this.rib.comptes.filter(c => c.idCompte !== this.featuredAccount?.idCompte);
  }
 
  copyToClipboard(value: string, field: string) {
    navigator.clipboard.writeText(value).then(() => {
      this.copiedField = field;
      setTimeout(() => this.copiedField = '', 2000);
    });
  }
 
  shareRib(compte: RibCompte) {
    const text = `NEO BTE — Coordonnées bancaires\nNom : ${this.rib?.nomComplet}\nRIB : ${compte.rib}\nE-mail : ${this.rib?.email}\nTéléphone : ${this.rib?.telephone}`;
    if (navigator.share) {
      navigator.share({ title: 'Mon RIB NEO BTE', text });
    } else {
      this.copyToClipboard(text, 'share-' + compte.idCompte);
    }
  }
 
  goBack() {
    if (this.selectedCompteId) {
      this.router.navigate(['/account', this.selectedCompteId]);
    } else {
      this.router.navigate(['/home-view']);
    }
  }
}