import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, Subscription, interval, debounceTime } from 'rxjs';
import { Compte, ACCOUNT_TYPE_META } from '../../../Entities/Interfaces/compte';
import { LoanProduct, Loan, LoanSimulation, LOAN_TYPE_LABELS, LOAN_STATUT_LABELS } from '../../../Entities/Interfaces/loan';
import { CompteService } from '../../../Services/compte-service';
import { LoanService } from '../../../Services/loan.service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';

type ViewStep = 'list' | 'new';

@Component({
  selector: 'app-loan-view',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './loan-view.html',
  styleUrl: './loan-view.css',
})
export class LoanView implements OnInit, OnDestroy {

  step: ViewStep = 'list';
  tab: 'active' | 'history' = 'active';

  products: LoanProduct[] = [];
  loans: Loan[] = [];
  comptes: Compte[] = [];

  selectedProduct: LoanProduct | null = null;
  montant: number | null = null;
  compteDestinationId: number | null = null;
  comptePrelevementId: number | null = null;
  motif = '';

  simulation: LoanSimulation | null = null;
  simulating = false;
  expandedLoanId: number | null = null;

  loading = true;
  submitting = false;
  error = '';
  success = '';

  private simSubject = new Subject<void>();
  private pollSub?: Subscription;
  private simSub?: Subscription;

  readonly loanTypeLabels = LOAN_TYPE_LABELS;
  readonly loanStatutLabels = LOAN_STATUT_LABELS;
  readonly accountTypeMeta = ACCOUNT_TYPE_META;

  constructor(
    private loanService: LoanService,
    private compteService: CompteService,
    private modalService: ConfirmModalService,
  ) { }

  ngOnInit() {
    this.loadAll();
    this.pollSub = interval(60000).subscribe(() => this.loadLoans());
    this.simSub = this.simSubject.pipe(debounceTime(400)).subscribe(() => this.runSimulation());
  }

  ngOnDestroy() { this.pollSub?.unsubscribe(); this.simSub?.unsubscribe(); }

  loadAll() {
    this.loading = true;
    this.loanService.getProducts().subscribe({
      next: (p) => { this.products = p; },
      error: () => { }
    });
    this.loanService.getMyLoans().subscribe({
      next: (l) => { this.loans = l; this.loading = false; },
      error: () => { this.loading = false; }
    });
    this.compteService.getMyAccounts().subscribe({
      next: (c) => this.comptes = (c ?? []).filter(a => a.statutCompte === 'ACTIVE'),
      error: () => { }
    });
  }

  loadLoans() {
    this.loanService.getMyLoans().subscribe({ next: (l) => this.loans = l, error: () => { } });
  }

  get activeLoans(): Loan[] { return this.loans.filter(l => ['ACTIVE', 'LATE', 'DEFAULT', 'PENDING_APPROVAL', 'APPROVED'].includes(l.statut)); }
  get historyLoans(): Loan[] { return this.loans.filter(l => ['PAID_OFF', 'REJECTED'].includes(l.statut)); }

  get totalMonthlyPayment(): number {
    return this.activeLoans.filter(l => l.statut === 'ACTIVE' || l.statut === 'LATE')
      .reduce((s, l) => s + l.mensualite, 0);
  }

  selectProduct(p: LoanProduct) {
    this.selectedProduct = p;
    this.montant = p.montantMin;
    this.simulation = null;
    this.triggerSim();
  }

  triggerSim() { this.simSubject.next(); }

  private runSimulation() {
    if (!this.selectedProduct || !this.montant) return;
    this.simulating = true;
    this.loanService.simulate(this.montant, this.selectedProduct.tauxAnnuel, this.selectedProduct.dureeEnMois)
      .subscribe({
        next: (s) => { this.simulation = s; this.simulating = false; },
        error: () => { this.simulating = false; }
      });
  }

  get canSubmit(): boolean {
    return !!(this.selectedProduct && this.montant && this.compteDestinationId && this.comptePrelevementId
      && this.montant >= this.selectedProduct.montantMin
      && this.montant <= this.selectedProduct.montantMax);
  }

  async submitLoan() {
    if (!this.selectedProduct || !this.montant || !this.compteDestinationId || !this.comptePrelevementId) return;
    const sim = this.simulation;
    const confirmed = await this.modalService.confirm({
      title: 'Confirmer la demande de prêt',
      message: sim
        ? `Demander ${this.fmt(this.montant)} TND sur ${this.selectedProduct.dureeEnMois} mois ? Mensualité estimée : ${this.fmt(sim.mensualite)} TND. Total à rembourser : ${this.fmt(sim.totalDu)} TND.`
        : `Demander ${this.fmt(this.montant)} TND — ${this.selectedProduct.nom} ?`,
      confirmText: 'Soumettre', cancelText: 'Annuler', type: 'warning'
    });
    if (!confirmed) return;

    this.submitting = true; this.error = '';
    this.loanService.requestLoan({
      productId: this.selectedProduct.id,
      compteDestinationId: this.compteDestinationId,
      comptePrelevementId: this.comptePrelevementId,
      montant: this.montant,
      motif: this.motif || undefined,
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.step = 'list';
        this.success = 'Demande soumise. Un administrateur l\'examinera prochainement.';
        this.selectedProduct = null; this.montant = null; this.simulation = null;
        this.loadAll();
        setTimeout(() => this.success = '', 6000);
      },
      error: (err) => { this.submitting = false; this.error = err?.error?.message || 'Échec de la demande.'; }
    });
  }

  toggleRepayments(id: number) { this.expandedLoanId = this.expandedLoanId === id ? null : id; }

  statutLabel(s: string): string { return this.loanStatutLabels[s as keyof typeof this.loanStatutLabels]?.label ?? s; }
  statutColor(s: string): string { return this.loanStatutLabels[s as keyof typeof this.loanStatutLabels]?.color ?? '#6b7280'; }
  typeLabel(t: string): string { return this.loanTypeLabels[t as keyof typeof this.loanTypeLabels] ?? t; }
  repayStatutLabel(s: string): string {
    const m: Record<string, string> = { PENDING: 'En attente', PAID: 'Payé', LATE: 'En retard', FAILED: 'Impayé', WAIVED: 'Exonéré' };
    return m[s] ?? s;
  }
  accountLabel(c: Compte): string {
    return `${this.accountTypeMeta[c.typeCompte]?.label ?? c.typeCompte} #${c.idCompte} · ${this.fmt(c.solde)} TND`;
  }
  durationLabel(m: number): string {
    if (m < 12) return `${m} mois`;
    const y = m / 12; return y === Math.floor(y) ? `${y} an${y > 1 ? 's' : ''}` : `${m} mois`;
  }
  fmt(n: number): string { return n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 }); }
  pct(n: number): string { return (n * 100).toFixed(2); }

  goNew() { this.step = 'new'; this.error = ''; this.selectedProduct = null; this.simulation = null; }
  goList() { this.step = 'list'; this.error = ''; }
}
