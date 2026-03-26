import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { CompteService } from '../../../Services/compte-service';
import { Compte } from '../../../Entities/Interfaces/compte';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { DemandeCloture } from '../../../Entities/Interfaces/demande-cloture';
import { FormsModule } from '@angular/forms';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-compte-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './compte-management.html',
  styleUrl: './compte-management.css',
})
export class CompteManagement implements OnInit, OnDestroy {

  private pollSub?: Subscription;

  activeTab: 'comptes' | 'clotures' = 'comptes';

  // Accounts
  comptes: Compte[] = [];
  accountsLoading = true;

  // Closure requests
  demandes: DemandeCloture[] = [];
  clotureFilter: 'EN_ATTENTE' | 'APPROUVEE' | 'REJETEE' | 'ALL' = 'EN_ATTENTE';
  cloturesLoading = true;

  // Inline action panel
  actionCompteId: number | null = null;
  actionType: 'status' | null = null;
  selectedStatut = '';
  actionCommentaire = '';
  actionLoading = false;
  actionError = '';

  // Closure decision panel
  clotureActionId: number | null = null;
  clotureActionType: 'approve' | 'reject' | null = null;
  clotureCommentaire = '';
  clotureSubmitting = false;
  clotureError = '';
  clotureOutcome: { blocked: boolean; message: string } | null = null;

  readonly statuts = ['ACTIVE', 'SUSPENDU', 'BLOQUE', 'CLOTURE'];

  constructor(
    private compteService: CompteService,
    private modalService: ConfirmModalService
  ) { }

  ngOnInit() {
    this.startPolling();
    this.loadComptes();
    this.loadClotures();
  }

  loadComptes() {
    this.accountsLoading = true;
    this.compteService.getAllAccounts().subscribe({
      next: (data) => { this.comptes = data; this.accountsLoading = false; },
      error: () => { this.accountsLoading = false; }
    });
  }

  loadClotures() {
    this.cloturesLoading = true;
    this.compteService.getAllDemandesCloture().subscribe({
      next: (data) => {
        this.demandes = this.clotureFilter === 'ALL'
          ? data
          : data.filter(d => d.statut === this.clotureFilter);
        this.cloturesLoading = false;
      },
      error: () => { this.cloturesLoading = false; }
    });
  }

  onClotureFilterChange() { this.loadClotures(); }

  // Status change
  openStatusAction(id: number, currentStatut: string) {
    this.actionCompteId = id;
    this.actionType = 'status';
    this.selectedStatut = currentStatut;
    this.actionCommentaire = '';
    this.actionError = '';
  }

  cancelAction() {
    this.actionCompteId = null;
    this.actionType = null;
    this.actionError = '';
  }

  async submitStatusChange() {
    if (!this.selectedStatut) return;

    const confirmed = await this.modalService.confirm({
      title: 'Confirmer le changement',
      message: `Changer le statut du compte vers ${this.selectedStatut} ?`,
      confirmText: 'Confirmer',
      cancelText: 'Annuler',
      type: 'warning'
    });
    if (!confirmed) return;

    this.actionLoading = true;
    this.compteService.updateStatutCompte(this.actionCompteId!, this.selectedStatut, this.actionCommentaire).subscribe({
      next: () => { this.actionLoading = false; this.cancelAction(); this.loadComptes(); },
      error: (err) => { this.actionError = err?.error?.message || 'Échec.'; this.actionLoading = false; }
    });
  }

  // Closure decisions
  openClotureAction(id: number, type: 'approve' | 'reject') {
    this.clotureActionId = id;
    this.clotureActionType = type;
    this.clotureCommentaire = '';
    this.clotureError = '';
  }

  cancelClotureAction() {
    this.clotureActionId = null;
    this.clotureActionType = null;
    this.clotureError = '';
  }

  async submitClotureAction() {
    if (this.clotureActionType === 'reject' && !this.clotureCommentaire.trim()) {
      this.clotureError = 'Un motif est requis pour rejeter.';
      return;
    }

    const confirmed = await this.modalService.confirm({
      title: this.clotureActionType === 'approve' ? 'Approuver la clôture' : 'Rejeter la demande',
      message: this.clotureActionType === 'approve'
        ? 'Approuver cette demande de clôture ? Si le solde est non nul, le compte sera bloqué.'
        : 'Rejeter cette demande de clôture ?',
      confirmText: this.clotureActionType === 'approve' ? 'Approuver' : 'Rejeter',
      cancelText: 'Annuler',
      type: this.clotureActionType === 'approve' ? 'warning' : 'danger'
    });
    if (!confirmed) return;

    this.clotureSubmitting = true;
    const call = this.clotureActionType === 'approve'
      ? this.compteService.approuverCloture(this.clotureActionId!, this.clotureCommentaire || undefined)
      : this.compteService.rejeterCloture(this.clotureActionId!, this.clotureCommentaire);

    call.subscribe({
      next: (res: any) => {
        this.clotureSubmitting = false;
        this.cancelClotureAction();
        // Check if account was blocked instead of closed (non-zero balance)
        const isBlocked = res?.commentaireAdmin?.includes('solde non nul');
        if (this.clotureActionType === 'approve') {
          this.clotureOutcome = isBlocked
            ? { blocked: true, message: `⚠️ Compte #${res.compteId} bloqué — solde non nul (${res.soldeAtDemande} TND). Le client doit vider son compte avant la clôture définitive.` }
            : { blocked: false, message: `✓ Compte #${res.compteId} clôturé avec succès.` };
        }
        this.loadClotures();
        this.loadComptes();
        setTimeout(() => this.clotureOutcome = null, 8000);
      },
      error: (err: any) => { this.clotureError = err?.error?.message || 'Échec.'; this.clotureSubmitting = false; }
    });
  }

  getStatutClass(s: string): string {
    switch (s) {
      case 'ACTIVE': return 'badge-green';
      case 'SUSPENDU': return 'badge-amber';
      case 'BLOQUE': return 'badge-amber';
      case 'CLOTURE': return 'badge-red';
      default: return 'badge-gray';
    }
  }

  getDemandeStatutClass(s: string): string {
    switch (s) {
      case 'EN_ATTENTE': return 'badge-yellow';
      case 'APPROUVEE': return 'badge-green';
      case 'REJETEE': return 'badge-red';
      default: return 'badge-gray';
    }
  }

  get pendingCloturesCount(): number {
    return this.demandes.filter(d => d.statut === 'EN_ATTENTE').length;
  }

  startPolling() {
    this.pollSub = interval(60000).subscribe(() => {
      this.loadComptes(); this.loadClotures();
    });
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

}