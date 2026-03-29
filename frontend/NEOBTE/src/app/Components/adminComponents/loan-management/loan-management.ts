import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoanProduct, Loan, LoanProductFormDTO, LOAN_TYPE_LABELS, LOAN_STATUT_LABELS, LoanType } from '../../../Entities/Interfaces/loan';
import { LoanService } from '../../../Services/loan.service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';

type AdminTab = 'products' | 'loans';

@Component({
  selector: 'app-loan-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './loan-management.html',
  styleUrl: './loan-management.css',
})
export class LoanManagement implements OnInit {

  tab: AdminTab = 'loans';
  products: LoanProduct[] = [];
  loans: Loan[] = [];
  loading = true;
  saving = false;
  error = '';
  success = '';

  showForm = false;
  editingProduct: LoanProduct | null = null;
  form: LoanProductFormDTO = this.emptyForm();

  statusFilter = 'ALL';
  rejectingId: number | null = null;
  rejectMotif = '';
  approvingId: number | null = null;
  approveNote = '';

  readonly loanTypeLabels = LOAN_TYPE_LABELS;
  readonly loanStatutLabels = LOAN_STATUT_LABELS;
  readonly loanTypes: LoanType[] = ['PERSONNEL', 'IMMOBILIER', 'AUTO', 'PROFESSIONNEL'];

  constructor(private loanService: LoanService, private modalService: ConfirmModalService) { }

  ngOnInit() { this.loadAll(); }

  loadAll() {
    this.loading = true;
    this.loanService.getAllProducts().subscribe({ next: (p) => { this.products = p; this.loading = false; }, error: () => { this.loading = false; } });
    this.loanService.getAllLoans().subscribe({ next: (l) => this.loans = l, error: () => { } });
  }

  get filteredLoans(): Loan[] {
    return this.statusFilter === 'ALL' ? this.loans : this.loans.filter(l => l.statut === this.statusFilter);
  }
  get pendingCount(): number { return this.loans.filter(l => l.statut === 'PENDING_APPROVAL').length; }
  get activeCount(): number { return this.loans.filter(l => ['ACTIVE', 'LATE'].includes(l.statut)).length; }
  get defaultCount(): number { return this.loans.filter(l => l.statut === 'DEFAULT').length; }

  openNew() { this.editingProduct = null; this.form = this.emptyForm(); this.showForm = true; this.error = ''; }
  openEdit(p: LoanProduct) { this.editingProduct = p; this.form = { ...p, tauxAnnuel: p.tauxAnnuel, penaltyRate: p.penaltyRate }; this.showForm = true; this.error = ''; }
  closeForm() { this.showForm = false; this.error = ''; }

  saveProduct() {
    this.saving = true; this.error = ''; this.success = '';
    const obs = this.editingProduct
      ? this.loanService.updateProduct(this.editingProduct.id, this.form)
      : this.loanService.createProduct(this.form);
    obs.subscribe({
      next: () => { this.saving = false; this.showForm = false; this.success = this.editingProduct ? 'Produit mis à jour.' : 'Produit créé.'; this.loadAll(); setTimeout(() => this.success = '', 3000); },
      error: (err: any) => { this.saving = false; this.error = err?.error?.message || 'Erreur.'; }
    });
  }

  async deleteProduct(p: LoanProduct) {
    const confirmed = await this.modalService.confirm({ title: 'Supprimer le produit', message: `Supprimer « ${p.nom} » ?`, confirmText: 'Supprimer', cancelText: 'Annuler', type: 'danger' });
    if (!confirmed) return;
    this.loanService.deleteProduct(p.id).subscribe({ next: () => { this.success = 'Supprimé.'; this.loadAll(); setTimeout(() => this.success = '', 3000); }, error: (err: any) => { this.error = err?.error?.message || 'Erreur.'; } });
  }

  startApprove(id: number) { this.approvingId = id; this.approveNote = ''; }
  cancelApprove() { this.approvingId = null; }
  confirmApprove() {
    if (!this.approvingId) return;
    this.loanService.approveLoan(this.approvingId, this.approveNote).subscribe({
      next: () => { this.approvingId = null; this.success = 'Prêt approuvé.'; this.loadAll(); setTimeout(() => this.success = '', 3000); },
      error: (err: any) => { this.error = err?.error?.message || 'Erreur approbation.'; this.approvingId = null; }
    });
  }

  startReject(id: number) { this.rejectingId = id; this.rejectMotif = ''; }
  cancelReject() { this.rejectingId = null; }
  confirmReject() {
    if (!this.rejectingId || !this.rejectMotif.trim()) return;
    this.loanService.rejectLoan(this.rejectingId, this.rejectMotif).subscribe({
      next: () => { this.rejectingId = null; this.success = 'Prêt refusé.'; this.loadAll(); setTimeout(() => this.success = '', 3000); },
      error: (err: any) => { this.error = err?.error?.message || 'Erreur refus.'; this.rejectingId = null; }
    });
  }

  pct(n: number): string { return (n * 100).toFixed(2); }
  setPct(field: 'tauxAnnuel' | 'penaltyRate', val: string) { (this.form as any)[field] = parseFloat(val) / 100; }
  fmt(n: number): string { return n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 }); }
  durationLabel(m: number): string { if (m < 12) return `${m} mois`; const y = m / 12; return y === Math.floor(y) ? `${y} an${y > 1 ? 's' : ''}` : `${m} mois`; }
  statutLabel(s: string): string { return this.loanStatutLabels[s as keyof typeof this.loanStatutLabels]?.label ?? s; }
  statutColor(s: string): string { return this.loanStatutLabels[s as keyof typeof this.loanStatutLabels]?.color ?? '#6b7280'; }
  typeLabel(t: string): string { return this.loanTypeLabels[t as keyof typeof this.loanTypeLabels] ?? t; }

  private emptyForm(): LoanProductFormDTO {
    return { nom: '', description: '', type: 'PERSONNEL', dureeEnMois: 12, tauxAnnuel: 0.12, montantMin: 500, montantMax: 30000, gracePeriodDays: 2, penaltyRate: 0.05, penaltyFixedFee: 15, defaultThreshold: 3, actif: true };
  }
}