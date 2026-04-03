import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { Compte, ACCOUNT_TYPE_META } from '../../../Entities/Interfaces/compte';
import { InvestmentPlan, Investment } from '../../../Entities/Interfaces/investment';
import { CompteService } from '../../../Services/compte-service';
import { InvestmentService } from '../../../Services/investment.service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';

type ViewStep = 'list' | 'new';

@Component({
  selector: 'app-investment-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './investment-view.html',
  styleUrl: './investment-view.css',
})
export class InvestmentView implements OnInit, OnDestroy {

  step: ViewStep = 'list';
  tab: 'active' | 'history' = 'active';

  plans: InvestmentPlan[] = [];
  investments: Investment[] = [];
  comptes: Compte[] = [];

  selectedPlan: InvestmentPlan | null = null;
  selectedCompteId: number | null = null;
  montant: number | null = null;

  loading = true;
  plansLoading = true;
  submitting = false;
  cancellingId: number | null = null;
  expandedId: number | null = null;

  error = '';
  success = '';

  private pollSub?: Subscription;
  readonly accountTypeMeta = ACCOUNT_TYPE_META;

  constructor(
    private investmentService: InvestmentService,
    private compteService: CompteService,
    private modalService: ConfirmModalService,
  ) { }

  ngOnInit() {
    this.loadAll();
    this.pollSub = interval(60000).subscribe(() => this.loadInvestments());
  }

  ngOnDestroy() { this.pollSub?.unsubscribe(); }

  loadAll() {
    this.loading = true;
    this.investmentService.getActivePlans().subscribe({
      next: (p) => { this.plans = p; this.plansLoading = false; },
      error: () => { this.plansLoading = false; }
    });
    this.investmentService.getMyInvestments().subscribe({
      next: (inv) => { this.investments = inv; this.loading = false; },
      error: () => { this.loading = false; }
    });
    this.compteService.getMyAccounts().subscribe({
      next: (c) => this.comptes = (c ?? []).filter(a => a.statutCompte === 'ACTIVE'),
      error: () => { }
    });
  }

  loadInvestments() {
    this.investmentService.getMyInvestments().subscribe({
      next: (inv) => this.investments = inv,
      error: () => { }
    });
  }

  get activeInvestments(): Investment[] {
    return this.investments.filter(i => i.statut === 'ACTIVE');
  }
  get historyInvestments(): Investment[] {
    return this.investments.filter(i => i.statut !== 'ACTIVE');
  }

  get totalLocked(): number {
    return this.activeInvestments.reduce((s, i) => s + i.montant, 0);
  }
  get totalExpectedReturn(): number {
    return this.activeInvestments.reduce((s, i) => s + i.interetAttendu, 0);
  }
  get totalCurrentValue(): number {
    return this.activeInvestments.reduce((s, i) => s + i.currentValue, 0);
  }

  selectPlan(plan: InvestmentPlan) {
    this.selectedPlan = plan;
    this.montant = plan.montantMin;
    this.error = '';
  }

  get estimatedReturn(): number {
    if (!this.selectedPlan || !this.montant) return 0;
    return Math.round(
      this.montant * this.selectedPlan.tauxAnnuel * (this.selectedPlan.dureeEnMois / 12) * 1000
    ) / 1000;
  }

  get selectedCompte(): Compte | null {
    return this.comptes.find(c => c.idCompte === this.selectedCompteId) ?? null;
  }

  get canSubmit(): boolean {
    return !!(this.selectedPlan && this.selectedCompteId && this.montant &&
      this.montant >= this.selectedPlan.montantMin &&
      this.montant <= this.selectedPlan.montantMax &&
      (this.selectedCompte?.solde ?? 0) >= this.montant);
  }

  async subscribe() {
    if (!this.selectedPlan || !this.selectedCompteId || !this.montant) return;
    const confirmed = await this.modalService.confirm({
      title: 'Confirmer le placement',
      message: `Investir ${this.fmt(this.montant)} TND dans le plan « ${this.selectedPlan.nom} » pendant ${this.selectedPlan.dureeEnMois} mois ? Le montant sera bloqué jusqu'à l'échéance.`,
      confirmText: 'Investir', cancelText: 'Annuler', type: 'warning'
    });
    if (!confirmed) return;

    this.submitting = true; this.error = '';
    this.investmentService.subscribe({
      planId: this.selectedPlan.id,
      compteId: this.selectedCompteId,
      montant: this.montant,
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.step = 'list';
        this.success = `Placement de ${this.fmt(this.montant!)} TND activé dans le plan « ${this.selectedPlan!.nom} ».`;
        this.selectedPlan = null; this.selectedCompteId = null; this.montant = null;
        this.loadAll();
        setTimeout(() => this.success = '', 5000);
      },
      error: (err) => {
        this.submitting = false;
        this.error = err?.error?.message || 'Échec de la souscription.';
      }
    });
  }

  async cancelInvestment(inv: Investment) {
    const confirmed = await this.modalService.confirm({
      title: 'Annuler le placement',
      message: `Annuler le placement « ${inv.planNom} » ? Vous récupérerez uniquement le capital (${this.fmt(inv.montant)} TND). Les intérêts seront perdus.`,
      confirmText: 'Annuler le placement', cancelText: 'Garder', type: 'danger'
    });
    if (!confirmed) return;

    this.cancellingId = inv.id;
    this.investmentService.cancel(inv.id).subscribe({
      next: () => {
        this.cancellingId = null;
        this.loadInvestments();
        this.compteService.getMyAccounts().subscribe({ next: (c) => this.comptes = (c ?? []).filter(a => a.statutCompte === 'ACTIVE') });
      },
      error: (err) => {
        this.cancellingId = null;
        this.error = err?.error?.message || 'Échec de l\'annulation.';
      }
    });
  }

  toggleBreakdown(id: number) {
    this.expandedId = this.expandedId === id ? null : id;
  }

  durationLabel(months: number): string {
    if (months < 12) return `${months} mois`;
    const y = months / 12;
    return y === Math.floor(y) ? `${y} an${y > 1 ? 's' : ''}` : `${months} mois`;
  }

  maturityDate(inv: Investment): string {
    return new Date(inv.dateEcheance).toLocaleDateString('fr-TN', { day: '2-digit', month: 'long', year: 'numeric' });
  }

  accountLabel(c: Compte): string {
    return `${this.accountTypeMeta[c.typeCompte]?.label ?? c.typeCompte} #${c.idCompte} · ${this.fmt(c.solde)} TND`;
  }

  fmt(n: number): string {
    return n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
  }

  pct(n: number): string { return (n * 100).toFixed(1); }

  goNew() { this.step = 'new'; this.error = ''; this.selectedPlan = null; this.montant = null; }
  goList() { this.step = 'list'; this.error = ''; }
}