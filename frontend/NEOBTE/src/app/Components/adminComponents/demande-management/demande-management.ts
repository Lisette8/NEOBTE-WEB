import { Component, OnDestroy, OnInit } from '@angular/core';
import { CompteService } from '../../../Services/compte-service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';

@Component({
  selector: 'app-demande-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demande-management.html',
  styleUrl: './demande-management.css',
})
export class DemandeManagement implements OnInit, OnDestroy {

  demandes: any[] = [];
  loading = true;
  filter: 'ALL' | 'EN_ATTENTE' | 'APPROUVEE' | 'REJETEE' = 'EN_ATTENTE';

  actionDemandeId: number | null = null;
  actionType: 'approve' | 'reject' | null = null;
  commentaire = '';
  submitting = false;
  actionError = '';

  constructor(
    private compteService: CompteService,
    private modalService: ConfirmModalService,
    private ws: WebsocketService
  ) { }

  ngOnInit() {
    this.loadDemandes();
    this.ws.subscribeAdmin((event) => {
      if (event.type === 'DEMANDE' || event.type === 'COMPTE') this.loadDemandes();
    });
  }

  ngOnDestroy() { }

  loadDemandes() {
    this.loading = true;
    const obs = this.filter === 'ALL'
      ? this.compteService.getAllDemandes()
      : this.compteService.getDemandesByStatut(this.filter);

    obs.subscribe({
      next: (data) => { this.demandes = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  onFilterChange() { this.loadDemandes(); }

  openAction(id: number, type: 'approve' | 'reject') {
    this.actionDemandeId = id;
    this.actionType = type;
    this.commentaire = '';
    this.actionError = '';
  }

  cancelAction() {
    this.actionDemandeId = null;
    this.actionType = null;
    this.actionError = '';
  }

  async submitAction() {
    if (this.actionType === 'reject' && !this.commentaire.trim()) {
      this.actionError = 'Un motif est requis pour rejeter.';
      return;
    }

    const confirmed = await this.modalService.confirm({
      title: this.actionType === 'approve' ? 'Approuver la demande' : 'Rejeter la demande',
      message: this.actionType === 'approve'
        ? 'Approuver cette demande d\'ouverture de compte ?'
        : 'Rejeter cette demande d\'ouverture de compte ?',
      confirmText: this.actionType === 'approve' ? 'Approuver' : 'Rejeter',
      cancelText: 'Annuler',
      type: this.actionType === 'approve' ? 'warning' : 'danger'
    });
    if (!confirmed) return;

    this.submitting = true;
    const call = this.actionType === 'approve'
      ? this.compteService.approveDemande(this.actionDemandeId!, this.commentaire)
      : this.compteService.rejectDemande(this.actionDemandeId!, this.commentaire);

    call.subscribe({
      next: () => { this.submitting = false; this.cancelAction(); this.loadDemandes(); },
      error: (err) => { this.actionError = err?.error?.message || 'Échec.'; this.submitting = false; }
    });
  }
}