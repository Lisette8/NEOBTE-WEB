import { Component, OnInit } from '@angular/core';
import { VirementService } from '../../../Services/virement.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Virement } from '../../../Entities/Interfaces/virement';
import { Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-virement-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './virement-view.html',
  styleUrl: './virement-view.css',
})


export class VirementView implements OnInit {

  comptes: Compte[] = [];
  virementForm: FormGroup;
  history: Virement[] = [];
  loading = false;
  message = '';
  error = '';
  historyFilter: 'all' | 'sent' | 'received' = 'all';

  get selectedCompte(): Compte | undefined {
    const id = this.virementForm.get('compteSourceId')?.value;
    return this.comptes.find(c => c.idCompte == id);
  }

  isSent(v: Virement): boolean {
    const id = this.virementForm.get('compteSourceId')?.value;
    return v.compteSourceId == id;
  }

  fieldInvalid(field: string): boolean {
    const ctrl = this.virementForm.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  get amountExceedsBalance(): boolean {
    const montant = this.virementForm.get('montant')?.value;
    return !!(this.selectedCompte && montant > this.selectedCompte.solde);
  }

  // #10 - filtered history based on active tab
  get filteredHistory(): Virement[] {
    if (this.historyFilter === 'sent')     return this.history.filter(v => this.isSent(v));
    if (this.historyFilter === 'received') return this.history.filter(v => !this.isSent(v));
    return this.history;
  }

  constructor(
    private virementService: VirementService,
    private compteService: CompteService,
    private fb: FormBuilder,
    private modalService: ConfirmModalService,
    private router: Router
  ) {
    this.virementForm = this.fb.group({
      compteSourceId: [null, [Validators.required]],
      compteDestinationId: [null, [Validators.required]],
      montant: [null, [Validators.required, Validators.min(1)]]
    });

    // Re-validate montant whenever source account changes (balance changes)
    this.virementForm.get('compteSourceId')?.valueChanges.subscribe(() => {
      this.virementForm.get('montant')?.updateValueAndValidity();
    });
  }

  ngOnInit() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user.id;

    this.compteService.getUserAccounts(userId)
      .subscribe(data => {
        this.comptes = data;
        // Pre-fill source account if navigated from compte-view
        const preselected = history.state?.['compteId'];
        if (preselected) {
          this.virementForm.get('compteSourceId')?.setValue(preselected);
        }
      });

    this.loadAccounts();

    // Auto-load history whenever source account changes
    this.virementForm.get('compteSourceId')?.valueChanges.subscribe(value => {
      if (value) {
        this.loadHistory();
      } else {
        this.history = [];
      }
    });
  }



  async transfer() {
    if (this.virementForm.invalid) {
      this.virementForm.markAllAsTouched();
      return;
    }

    if (this.virementForm.value.compteSourceId === this.virementForm.value.compteDestinationId) {
      this.error = "Vous ne pouvez pas effectuer un virement vers le même compte";
      return;
    }

    if (this.amountExceedsBalance) {
      this.error = "Solde insuffisant pour ce virement";
      return;
    }

    const { montant, compteSourceId, compteDestinationId } = this.virementForm.value;

    const confirmed = await this.modalService.confirm({
      title: 'Confirmer le virement',
      message: `Vous êtes sur le point de transférer ${montant} TND du compte #${compteSourceId} vers le compte #${compteDestinationId}. Cette action est irréversible.`,
      confirmText: 'Envoyer',
      cancelText: 'Annuler',
      type: 'warning'
    });

    if (!confirmed) return;

    this.loading = true;
    this.message = '';
    this.error = '';

    const request = {
      ...this.virementForm.value,
      idempotencyKey: crypto.randomUUID() //randomUUID generates random id for the transfer
    }

    this.virementService.transfer(request).subscribe({
      next: (res) => {
        this.loading = false;
        // #8 - preserve source account, only reset destination and amount
        const sourceId = this.virementForm.get('compteSourceId')?.value;
        this.virementForm.reset();
        this.virementForm.get('compteSourceId')?.setValue(sourceId);
        this.loadAccounts();
        this.loadHistory();
        console.log(res);
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
      }
    });
  }


  //loaders , i created these methods to make sure everything loads and refreshes automatically 
  loadHistory() {
    const sourceId = this.virementForm.get('compteSourceId')?.value;
    if (!sourceId) return;

    this.loading = true;
    this.virementService.getHistory(sourceId).subscribe({
      next: (data) => {
        this.history = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = "Erreur lors du chargement de l'historique";
      }
    });
  }


  loadAccounts() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user.id;

    this.compteService.getUserAccounts(userId)
      .subscribe(data => {
        this.comptes = data;
      });

  }
}