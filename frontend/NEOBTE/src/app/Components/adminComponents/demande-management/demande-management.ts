import { Component, OnDestroy, OnInit } from '@angular/core';
import { CompteService } from '../../../Services/compte-service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-demande-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demande-management.html',
  styleUrl: './demande-management.css',
})
export class DemandeManagement implements OnInit, OnDestroy {

  private pollSub?: Subscription;

  demandes: any[] = [];
  loading = true;
  filter: 'ALL' | 'EN_ATTENTE' | 'APPROUVEE' | 'REJETEE' = 'EN_ATTENTE';

  // Inline decision panel
  actionDemandeId: number | null = null;
  actionType: 'approve' | 'reject' | null = null;
  commentaire = '';
  submitting = false;
  actionError = '';

  constructor(
    private compteService: CompteService,
    private modalService: ConfirmModalService,
  ) { }

  ngOnInit() { this.loadDemandes(); }

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
    this.commentaire = '';
    this.actionError = '';
  }

  async submitAction() {
    if (this.actionType === 'reject' && !this.commentaire.trim()) {
      this.actionError = 'A reason is required when rejecting.';
      return;
    }

    const label = this.actionType === 'approve' ? 'approve' : 'reject';
    const confirmed = await this.modalService.confirm({
      title: `Confirm ${label}`,
      message: `Are you sure you want to ${label} this account request?`,
      confirmText: label.charAt(0).toUpperCase() + label.slice(1),
      cancelText: 'Cancel',
      type: this.actionType === 'approve' ? 'warning' : 'danger',
    });

    if (!confirmed) return;

    this.submitting = true;
    this.actionError = '';

    const call = this.actionType === 'approve'
      ? this.compteService.approveDemande(this.actionDemandeId!, this.commentaire)
      : this.compteService.rejectDemande(this.actionDemandeId!, this.commentaire);

    call.subscribe({
      next: () => {
        this.submitting = false;
        this.cancelAction();
        this.loadDemandes();
      },
      error: (err) => {
        this.submitting = false;
        this.actionError = err?.error?.message || 'Action failed.';
      }
    });
  }

  getStatusClass(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'badge-yellow';
      case 'APPROUVEE': return 'badge-green';
      case 'REJETEE': return 'badge-red';
      default: return 'badge-gray';
    }
  }

  getStatusLabel(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'Pending';
      case 'APPROUVEE': return 'Approved';
      case 'REJETEE': return 'Rejected';
      default: return statut;
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'COURANT': return '🏦';
      case 'EPARGNE': return '💰';
      case 'PROFESSIONNEL': return '💼';
      default: return '💳';
    }
  }

  startPolling() {
    this.pollSub = interval(3000).subscribe(() => {
      this.loadDemandes();
    });
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

}