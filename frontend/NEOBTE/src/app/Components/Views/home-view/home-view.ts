import { Component, OnInit } from '@angular/core';
import { ActualiteService } from '../../../Services/actualite-service';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Compte } from '../../../Entities/Interfaces/compte';
import { DemandeCompte } from '../../../Entities/Interfaces/demande-compte';
import { AuthService } from '../../../Services/auth-service';
import { CompteService } from '../../../Services/compte-service';

@Component({
  selector: 'app-home-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home-view.html',
  styleUrl: './home-view.css',
})
export class HomeView implements OnInit {
 
  userName = '';
  comptes: Compte[] = [];
  demandes: DemandeCompte[] = [];
  actualites: Actualite[] = [];
  loading = true;
 
  constructor(
    private authService: AuthService,
    private compteService: CompteService,
    private actualiteService: ActualiteService,
    private router: Router
  ) {}
 
  ngOnInit() {
    const userId = this.authService.getUserId();
    if (!userId) return;
 
    this.authService.getCurrentUser().subscribe({
      next: (user) => this.userName = user.prenom || '',
      error: () => {}
    });
 
    this.compteService.getUserAccounts(userId).subscribe({
      next: (data) => { this.comptes = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
 
    this.compteService.getMyDemandes().subscribe({
      next: (data) => this.demandes = data,
      error: () => {}
    });
 
    this.actualiteService.getAll(0, 3).subscribe({
      next: (data) => this.actualites = data.content,
      error: () => {}
    });
  }
 
  // Accounts shown on dashboard — hide CLOSED and accounts pending deletion
  get visibleComptes(): Compte[] {
    return this.comptes.filter(c =>
      c.statutCompte !== 'CLOSED' &&
      !(c.statutCompte === 'SUSPENDED' && c.dateSuppressionPrevue)
    );
  }
 
  get totalBalance(): number {
    return this.visibleComptes
      .filter(c => c.statutCompte === 'ACTIVE')
      .reduce((sum, c) => sum + (c.solde ?? 0), 0);
  }
 
  get activeComptes(): Compte[] {
    return this.visibleComptes.filter(c => c.statutCompte === 'ACTIVE');
  }
 
  get pendingDemandes(): DemandeCompte[] {
    return this.demandes.filter(d => d.statutDemande === 'EN_ATTENTE');
  }
 
  openAccount(compteId: number) {
    this.router.navigate(['/account', compteId]);
  }
 
  getAccountLabel(type: string): string {
    switch (type) {
      case 'COURANT':       return 'Compte Chèque';
      case 'EPARGNE':       return 'Compte Épargne';
      case 'PROFESSIONNEL': return 'Compte Pro';
      default:              return type;
    }
  }
 
  getTimeOfDay(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Bonjour';
    if (h < 18) return 'Bon après-midi';
    return 'Bonsoir';
  }
}