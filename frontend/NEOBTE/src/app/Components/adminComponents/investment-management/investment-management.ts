import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InvestmentPlan, Investment, InvestmentPlanFormDTO } from '../../../Entities/Interfaces/investment';
import { InvestmentService } from '../../../Services/investment.service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';

type AdminTab = 'plans' | 'investments';

@Component({
  selector: 'app-investment-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './investment-management.html',
  styleUrl: './investment-management.css',
})
export class InvestmentManagement implements OnInit {

  tab: AdminTab = 'plans';
  plans: InvestmentPlan[] = [];
  investments: Investment[] = [];
  loading = true;
  saving = false;
  error = '';
  success = '';

  // Plan form
  editingPlan: InvestmentPlan | null = null;
  showForm = false;
  form: InvestmentPlanFormDTO = this.emptyForm();

  // Filter
  statusFilter: 'ALL' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED' = 'ALL';

  constructor(
    private investmentService: InvestmentService,
    private modalService: ConfirmModalService,
  ) { }

  ngOnInit() { this.loadAll(); }

  loadAll() {
    this.loading = true;
    this.investmentService.getAllPlans().subscribe({
      next: (p) => { this.plans = p; this.loading = false; },
      error: () => { this.loading = false; }
    });
    this.investmentService.getAllInvestments().subscribe({
      next: (i) => this.investments = i,
      error: () => { }
    });
  }

  get filteredInvestments(): Investment[] {
    if (this.statusFilter === 'ALL') return this.investments;
    return this.investments.filter(i => i.statut === this.statusFilter);
  }

  get totalVolume(): number { return this.investments.filter(i => i.statut === 'ACTIVE').reduce((s, i) => s + i.montant, 0); }
  get activeCount(): number { return this.investments.filter(i => i.statut === 'ACTIVE').length; }
  get completedCount(): number { return this.investments.filter(i => i.statut === 'COMPLETED').length; }

  openNewPlan() { this.editingPlan = null; this.form = this.emptyForm(); this.showForm = true; this.error = ''; }
  openEditPlan(plan: InvestmentPlan) { this.editingPlan = plan; this.form = { ...plan, tauxAnnuel: plan.tauxAnnuel }; this.showForm = true; this.error = ''; }
  closeForm() { this.showForm = false; this.editingPlan = null; this.error = ''; }

  savePlan() {
    this.saving = true; this.error = ''; this.success = '';
    const obs = this.editingPlan
      ? this.investmentService.updatePlan(this.editingPlan.id, this.form)
      : this.investmentService.createPlan(this.form);
    obs.subscribe({
      next: () => {
        this.saving = false; this.showForm = false;
        this.success = this.editingPlan ? 'Plan mis à jour.' : 'Plan créé.';
        this.loadAll();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err: any) => { this.saving = false; this.error = err?.error?.message || 'Erreur.'; }
    });
  }

  async deletePlan(plan: InvestmentPlan) {
    const confirmed = await this.modalService.confirm({
      title: 'Supprimer le plan',
      message: `Supprimer « ${plan.nom} » ? Si des placements sont actifs, il sera désactivé.`,
      confirmText: 'Supprimer', cancelText: 'Annuler', type: 'danger'
    });
    if (!confirmed) return;
    this.investmentService.deletePlan(plan.id).subscribe({
      next: () => { this.success = 'Plan supprimé.'; this.loadAll(); setTimeout(() => this.success = '', 3000); },
      error: (err: any) => { this.error = err?.error?.message || 'Erreur.'; }
    });
  }

  pct(n: number): string { return (n * 100).toFixed(2); }
  setPct(val: string) { this.form.tauxAnnuel = parseFloat(val) / 100; }

  durationLabel(months: number): string {
    if (months < 12) return `${months} mois`;
    const y = months / 12;
    return y === Math.floor(y) ? `${y} an${y > 1 ? 's' : ''}` : `${months} mois`;
  }

  fmt(n: number): string { return n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 }); }

  private emptyForm(): InvestmentPlanFormDTO {
    return { nom: '', description: '', dureeEnMois: 6, tauxAnnuel: 0.05, montantMin: 500, montantMax: 50000, actif: true };
  }
}