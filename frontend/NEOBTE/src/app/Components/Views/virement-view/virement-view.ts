import { Component, OnInit } from '@angular/core';
import { VirementService } from '../../../Services/virement.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Virement } from '../../../Entities/Interfaces/virement';
import { Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { RecipientPreview } from '../../../Entities/Interfaces/recipient-preview';
import { TransferConstraints } from '../../../Entities/Interfaces/transfer-constraints';

@Component({
  selector: 'app-virement-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './virement-view.html',
  styleUrl: './virement-view.css',
})
export class VirementView implements OnInit {
 
  step: 'lookup' | 'confirm' | 'success' = 'lookup';
  mode: 'externe' | 'interne' = 'externe';
  transferForm: FormGroup;
  internalForm: FormGroup;
  recipient: RecipientPreview | null = null;
  resolving = false;
  resolveError = '';
  lastCompletedTransfer: Virement | null = null;
 
  private identifierChange$ = new Subject<string>();
 
  loading = false;
  error = '';
  limitWarning = '';  // shown inline before submit, not after
  history: Virement[] = [];
  historyLoading = false;

  comptes: Compte[] = [];
  comptesLoading = false;
  constraintsExternal: TransferConstraints | null = null;
  constraintsInternal: TransferConstraints | null = null;
 
  constructor(
    private virementService: VirementService,
    private compteService: CompteService,
    private fb: FormBuilder,
    private modalService: ConfirmModalService,
  ) {
    this.transferForm = this.fb.group({
      recipientIdentifier: ['', Validators.required],
      montant: [null, [Validators.required, Validators.min(1)]],
    });

    this.internalForm = this.fb.group({
      compteSourceId: [null, Validators.required],
      compteDestinationId: [null, Validators.required],
      montant: [null, [Validators.required, Validators.min(1)]],
    });
  }
 
  ngOnInit() {
    this.loadHistory();
    this.loadComptes();
    this.loadConstraints();
 
    this.identifierChange$.pipe(
      debounceTime(600),
      distinctUntilChanged(),
      switchMap(identifier => {
        if (!identifier || identifier.length < 5) {
          this.recipient = null;
          this.resolveError = '';
          this.resolving = false;
          return [];
        }
        this.resolving = true;
        this.resolveError = '';
        return this.virementService.resolveRecipient(identifier);
      })
    ).subscribe({
      next: (preview) => {
        this.resolving = false;
        if (preview.found) {
          this.recipient = preview;
          this.resolveError = '';
          this.checkLimitWarning();
        } else {
          this.recipient = null;
          this.resolveError = 'Aucun compte trouvé avec cet e-mail ou ce numéro de téléphone.';
        }
      },
      error: () => {
        this.resolving = false;
        this.recipient = null;
        this.resolveError = 'Impossible de rechercher le destinataire. Réessayez.';
      }
    });
 
    // Re-check limit warning whenever the amount changes
    this.transferForm.get('montant')?.valueChanges.subscribe(() => this.checkLimitWarning());
    this.internalForm.get('montant')?.valueChanges.subscribe(() => this.checkLimitWarning());
  }

  setMode(mode: 'externe' | 'interne') {
    this.mode = mode;
    this.step = 'lookup';
    this.error = '';
    this.resolveError = '';
    this.limitWarning = '';
    this.recipient = null;
    this.transferForm.reset();
    this.internalForm.reset();
  }

  private loadComptes() {
    this.comptesLoading = true;
    this.compteService.getMyAccounts().subscribe({
      next: (accounts) => {
        this.comptes = accounts ?? [];
        this.comptesLoading = false;
      },
      error: () => {
        this.comptes = [];
        this.comptesLoading = false;
      },
    });
  }

  private loadConstraints() {
    this.virementService.getConstraints(false).subscribe({
      next: (c) => (this.constraintsExternal = c),
      error: () => (this.constraintsExternal = null),
    });
    this.virementService.getConstraints(true).subscribe({
      next: (c) => (this.constraintsInternal = c),
      error: () => (this.constraintsInternal = null),
    });
  }
 
  onIdentifierInput(event: Event) {
    this.identifierChange$.next((event.target as HTMLInputElement).value);
  }
 
  fieldInvalid(field: string): boolean {
    const form = this.mode === 'interne' ? this.internalForm : this.transferForm;
    const ctrl = form.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
 
  get montant(): number {
    const form = this.mode === 'interne' ? this.internalForm : this.transferForm;
    return form.get('montant')?.value ?? 0;
  }

  get compteSourceId(): number | null {
    return this.internalForm.get('compteSourceId')?.value ?? null;
  }
  get compteDestinationId(): number | null {
    return this.internalForm.get('compteDestinationId')?.value ?? null;
  }

  get selectedSource(): Compte | null {
    return this.compteSourceId ? this.comptes.find((c) => c.idCompte === this.compteSourceId) ?? null : null;
  }
  get selectedDestination(): Compte | null {
    return this.compteDestinationId ? this.comptes.find((c) => c.idCompte === this.compteDestinationId) ?? null : null;
  }

  private get currentConstraints(): TransferConstraints | null {
    if (this.mode === 'interne') return this.constraintsInternal;
    if (this.recipient) {
      return {
        feeRate: this.recipient.feeRate ?? 0,
        largeTransferThreshold: this.recipient.largeTransferThreshold ?? null,
        dailyAmountLimit: this.recipient.dailyAmountLimit ?? null,
        dailyCountLimit: this.recipient.dailyCountLimit ?? null,
      };
    }
    return this.constraintsExternal;
  }
 
  get estimatedFee(): number {
    const c = this.currentConstraints;
    if (!c || !this.montant) return 0;
    return Math.round(this.montant * (c.feeRate ?? 0) * 1000) / 1000;
  }
 
  get totalDebite(): number { return this.montant + this.estimatedFee; }

  mediaUrl(url?: string | null): string {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`;
  }
 
  /** Check limits and set a warning message — does NOT block the UI, just informs */
  checkLimitWarning() {
    this.limitWarning = '';
    const c = this.currentConstraints;
    if (!c || !this.montant) return;
 
    const threshold = c.largeTransferThreshold;
    const dailyAmount = c.dailyAmountLimit;
    const dailyCount = c.dailyCountLimit;
 
    if (threshold && this.montant > threshold) {
      this.limitWarning = `Ce montant dépasse la limite par virement de ${this.formatNum(threshold)} TND fixée par votre banque.`;
      return;
    }
    if (dailyAmount && this.montant > dailyAmount) {
      this.limitWarning = `Ce montant dépasse votre limite journalière de ${this.formatNum(dailyAmount)} TND.`;
      return;
    }
    if (dailyCount) {
      this.limitWarning = `Limite journalière : ${dailyCount} virements maximum.`;
    }
  }
 
  get hasLimitError(): boolean {
    const c = this.currentConstraints;
    if (!c || !this.montant) return false;
    const threshold = c.largeTransferThreshold;
    return !!(threshold && this.montant > threshold);
  }
 
  proceedToConfirm() {
    this.error = '';
    if (this.mode === 'interne') {
      if (this.internalForm.invalid) {
        Object.values(this.internalForm.controls).forEach((c) => c.markAsTouched());
        return;
      }
      if (this.compteSourceId && this.compteDestinationId && this.compteSourceId === this.compteDestinationId) {
        this.error = 'Veuillez sélectionner deux comptes différents.';
        return;
      }
    } else {
      if (!this.recipient) { this.resolveError = 'Veuillez saisir un destinataire valide.'; return; }
      if (!this.montant || this.transferForm.get('montant')?.invalid) {
        this.transferForm.get('montant')?.markAsTouched(); return;
      }
    }
    if (this.hasLimitError) {
      this.error = this.limitWarning;
      return;
    }
    this.step = 'confirm';
  }
 
  async confirmTransfer() {
    if (this.mode === 'interne') {
      const confirmed = await this.modalService.confirm({
        title: 'Confirmer le transfert interne',
        message: `Transférer ${this.montant} TND du compte #${this.compteSourceId} vers le compte #${this.compteDestinationId} ?`,
        confirmText: 'Transférer',
        cancelText: 'Annuler',
        type: 'warning'
      });
      if (!confirmed) return;

      this.loading = true;
      this.error = '';
      this.virementService.transferInterne({
        compteSourceId: this.compteSourceId!,
        compteDestinationId: this.compteDestinationId!,
        montant: this.montant,
        idempotencyKey: crypto.randomUUID(),
      }).subscribe({
        next: (result) => {
          this.loading = false;
          this.lastCompletedTransfer = result;
          this.step = 'success';
          this.loadHistory();
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error?.message || 'Transfert échoué. Veuillez réessayer.';
          this.step = 'confirm';
        }
      });
      return;
    }

    const confirmed = await this.modalService.confirm({
      title: 'Confirmer le virement',
      message: `Envoyer ${this.montant} TND à ${this.recipient!.displayName} ? Total débité : ${this.totalDebite} TND (dont ${this.estimatedFee} TND de frais).`,
      confirmText: 'Envoyer',
      cancelText: 'Annuler',
      type: 'warning'
    });
 
    if (!confirmed) return;
 
    this.loading = true;
    this.error = '';
 
    this.virementService.transfer({
      recipientIdentifier: this.transferForm.value.recipientIdentifier,
      montant: this.montant,
      idempotencyKey: crypto.randomUUID(),
    }).subscribe({
      next: (result) => {
        this.loading = false;
        this.lastCompletedTransfer = result;
        this.step = 'success';
        this.loadHistory();
      },
      error: (err) => {
        this.loading = false;
        // Backend sends a clear message — show it directly, no generic fallback
        this.error = err?.error?.message || 'Virement échoué. Veuillez réessayer.';
        this.step = 'confirm'; // stay on confirm so user can see the error
      }
    });
  }
 
  resetForm() {
    this.step = 'lookup';
    this.recipient = null;
    this.resolveError = '';
    this.error = '';
    this.limitWarning = '';
    this.lastCompletedTransfer = null;
    this.transferForm.reset();
  }
 
  loadHistory() {
    this.historyLoading = true;
    this.virementService.getHistory().subscribe({
      next: (data) => { this.history = data; this.historyLoading = false; },
      error: () => { this.historyLoading = false; }
    });
  }
 
  private formatNum(n: number): string {
    return n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
  }
}
