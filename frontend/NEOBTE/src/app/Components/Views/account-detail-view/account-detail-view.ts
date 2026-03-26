import { Component, OnDestroy, OnInit } from '@angular/core';
import { Virement } from '../../../Entities/Interfaces/virement';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ACCOUNT_TYPE_META, Compte } from '../../../Entities/Interfaces/compte';
import { CommonModule } from '@angular/common';
import { CompteService } from '../../../Services/compte-service';
import { VirementService } from '../../../Services/virement.service';
import { AuthService } from '../../../Services/auth-service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { FormsModule } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';
import { BalanceChart } from '../../balance-chart/balance-chart';
import { AccountPhysicalCard } from '../../account-physical-card/account-physical-card';

@Component({
  selector: 'app-account-detail-view',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, BalanceChart, AccountPhysicalCard],
  templateUrl: './account-detail-view.html',
  styleUrl: './account-detail-view.css',
})
export class AccountDetailView implements OnInit, OnDestroy {

  compte: Compte | null = null;
  history: Virement[] = [];
  loading = true;
  historyLoading = true;

  private pollSub?: Subscription;
  private readonly POLL_INTERVAL = 30_000; // 30s live refresh

  showClotureForm = false;
  clotureMotif = '';
  actionLoading = false;
  actionError = '';
  actionSuccess = '';

  private compteId!: number;
  private userId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private compteService: CompteService,
    private virementService: VirementService,
    private authService: AuthService,
    private modalService: ConfirmModalService,
  ) { }

  ngOnInit() {
    this.compteId = Number(this.route.snapshot.paramMap.get('id'));
    this.userId = this.authService.getUserId()!;
    if (!this.userId) { this.router.navigate(['/auth-view']); return; }
    this.loadCompte();
    this.startPolling();
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  private startPolling() {
    this.pollSub = interval(this.POLL_INTERVAL).pipe(
      startWith(0),
      switchMap(() => this.virementService.getHistory())
    ).subscribe({
      next: (data) => {
        const filtered = data.filter(v =>
          v.compteSourceId === this.compteId || v.compteDestinationId === this.compteId
        );
        if (filtered.length !== this.history.length) {
          this.loadCompte();
        }
        this.history = filtered;
        this.historyLoading = false;
      },
      error: () => { this.historyLoading = false; }
    });
  }

  loadCompte() {
    this.loading = true;
    this.compteService.getUserAccounts(this.userId).subscribe({
      next: (accounts) => {
        this.compte = accounts.find(a => a.idCompte === this.compteId) ?? null;
        this.loading = false;
        if (!this.compte) this.router.navigate(['/home-view']);
      },
      error: () => { this.loading = false; }
    });
  }

  get accountLabel(): string {
    switch (this.compte?.typeCompte) {
      case 'COURANT': return 'Compte Chèque';
      case 'EPARGNE': return 'Compte Épargne';
      case 'PROFESSIONNEL': return 'Compte Professionnel';
      default: return this.compte?.typeCompte ?? '';
    }
  }

  get statutLabel(): string {
    switch (this.compte?.statutCompte) {
      case 'ACTIVE': return 'Actif';
      case 'SUSPENDED': return this.compte?.dateSuppressionPrevue ? 'Clôture planifiée' : 'En pause';
      case 'BLOCKED': return 'Bloqué';
      case 'CLOSED': return 'Clôturé';
      default: return this.compte?.statutCompte ?? '';
    }
  }

  isOutgoing(v: Virement): boolean {
    return v.compteSourceId === this.compte?.idCompte;
  }

  sendMoney() {
    if (this.compte?.typeCompte === 'EPARGNE') {
      // Savings accounts can only do internal transfers
      this.router.navigate(['/virement-view'], { queryParams: { mode: 'interne' } });
    } else {
      this.router.navigate(['/virement-view']);
    }
  }
  receiveMoney() { this.router.navigate(['/receive'], { queryParams: { compteId: this.compteId } }); }
  goBack() { this.router.navigate(['/home-view']); }

  suspendre() {
    this.actionLoading = true;
    this.actionError = '';
    this.actionSuccess = '';
    this.compteService.suspendreCompte(this.compteId).subscribe({
      next: () => {
        this.actionSuccess = 'Compte mis en pause.';
        this.actionLoading = false;
        this.loadCompte();
      },
      error: (err) => {
        this.actionError = err?.error?.message || 'Échec. Réessayez.';
        this.actionLoading = false;
      }
    });
  }

  reactiver() {
    this.actionLoading = true;
    this.actionError = '';
    this.actionSuccess = '';
    this.compteService.reactiverCompte(this.compteId).subscribe({
      next: () => {
        this.actionSuccess = 'Compte réactivé avec succès !';
        this.actionLoading = false;
        this.loadCompte();
      },
      error: (err) => {
        this.actionError = err?.error?.message || 'Échec. Réessayez.';
        this.actionLoading = false;
      }
    });
  }

  submitCloture() {
    if (!this.clotureMotif.trim()) return;
    this.actionLoading = true;
    this.actionError = '';
    this.actionSuccess = '';
    this.compteService.demanderCloture(this.compteId, this.clotureMotif).subscribe({
      next: () => {
        this.actionSuccess = 'Demande soumise. Vous avez 48h pour annuler.';
        this.showClotureForm = false;
        this.clotureMotif = '';
        this.actionLoading = false;
        this.loadCompte();
      },
      error: (err) => {
        this.actionError = err?.error?.message || 'Échec de la demande.';
        this.actionLoading = false;
      }
    });
  }

  annulerCloture() {
    this.actionLoading = true;
    this.actionError = '';
    this.actionSuccess = '';
    this.compteService.annulerCloture(this.compteId).subscribe({
      next: () => {
        this.actionSuccess = 'Demande de clôture annulée. Votre compte est réactivé.';
        this.actionLoading = false;
        this.loadCompte();
      },
      error: (err) => {
        this.actionError = err?.error?.message || 'Échec. Réessayez.';
        this.actionLoading = false;
      }
    });
  }
  get accountTypeLabel(): string {
    return ACCOUNT_TYPE_META[this.compte?.typeCompte ?? '']?.label ?? this.compte?.typeCompte ?? '';
  }

  get accountTypePurpose(): string {
    return ACCOUNT_TYPE_META[this.compte?.typeCompte ?? '']?.purpose ?? '';
  }

  get accountTypeIcon(): string {
    return ACCOUNT_TYPE_META[this.compte?.typeCompte ?? '']?.icon ?? '🏦';
  }

  get accountTypeColor(): string {
    return ACCOUNT_TYPE_META[this.compte?.typeCompte ?? '']?.color ?? '#6b7280';
  }

}