import { Component, OnInit } from '@angular/core';
import { VirementService } from '../../../Services/virement.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Virement } from '../../../Entities/Interfaces/virement';
import { Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { AuthService } from '../../../Services/auth-service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { RecipientPreview } from '../../../Entities/Interfaces/recipient-preview';

@Component({
  selector: 'app-virement-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './virement-view.html',
  styleUrl: './virement-view.css',
})
export class VirementView implements OnInit {
 
  step: 'lookup' | 'confirm' | 'success' = 'lookup';
  transferForm: FormGroup;
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
 
  constructor(
    private virementService: VirementService,
    private fb: FormBuilder,
    private modalService: ConfirmModalService,
  ) {
    this.transferForm = this.fb.group({
      recipientIdentifier: ['', Validators.required],
      montant: [null, [Validators.required, Validators.min(1)]],
    });
  }
 
  ngOnInit() {
    this.loadHistory();
 
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
  }
 
  onIdentifierInput(event: Event) {
    this.identifierChange$.next((event.target as HTMLInputElement).value);
  }
 
  fieldInvalid(field: string): boolean {
    const ctrl = this.transferForm.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
 
  get montant(): number { return this.transferForm.get('montant')?.value ?? 0; }
 
  get estimatedFee(): number {
    if (!this.recipient || !this.montant) return 0;
    return Math.round(this.montant * (this.recipient.feeRate ?? 0.005) * 1000) / 1000;
  }
 
  get totalDebite(): number { return this.montant + this.estimatedFee; }
 
  /** Check limits and set a warning message — does NOT block the UI, just informs */
  checkLimitWarning() {
    this.limitWarning = '';
    if (!this.recipient || !this.montant) return;
 
    const threshold = this.recipient.largeTransferThreshold;
    const dailyAmount = this.recipient.dailyAmountLimit;
    const dailyCount = this.recipient.dailyCountLimit;
 
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
    if (!this.recipient || !this.montant) return false;
    const threshold = this.recipient.largeTransferThreshold;
    return !!(threshold && this.montant > threshold);
  }
 
  proceedToConfirm() {
    this.error = '';
    if (!this.recipient) { this.resolveError = 'Veuillez saisir un destinataire valide.'; return; }
    if (!this.montant || this.transferForm.get('montant')?.invalid) {
      this.transferForm.get('montant')?.markAsTouched(); return;
    }
    if (this.hasLimitError) {
      this.error = this.limitWarning;
      return;
    }
    this.step = 'confirm';
  }
 
  async confirmTransfer() {
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