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
        if (preview.found) { this.recipient = preview; this.resolveError = ''; }
        else { this.recipient = null; this.resolveError = 'No account found with that email or phone number.'; }
      },
      error: () => { this.resolving = false; this.recipient = null; this.resolveError = 'Could not look up recipient.'; }
    });
  }
 
  onIdentifierInput(event: Event) {
    this.identifierChange$.next((event.target as HTMLInputElement).value);
  }
 
  fieldInvalid(field: string): boolean {
    const ctrl = this.transferForm.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
 
  get montant(): number { return this.transferForm.get('montant')?.value ?? 0; }
 
  // Compute fee from recipient's feeRate (provided by backend on resolve)
  get estimatedFee(): number {
    if (!this.recipient || !this.montant) return 0;
    const rate = this.recipient.feeRate ?? 0.005;
    return Math.round(this.montant * rate * 1000) / 1000;
  }
 
  get totalDebite(): number { return this.montant + this.estimatedFee; }
 
  proceedToConfirm() {
    this.error = '';
    if (!this.recipient) { this.resolveError = 'Please enter a valid recipient.'; return; }
    if (!this.montant || this.transferForm.get('montant')?.invalid) {
      this.transferForm.get('montant')?.markAsTouched(); return;
    }
    this.step = 'confirm';
  }
 
  async confirmTransfer() {
    const confirmed = await this.modalService.confirm({
      title: 'Confirm Transfer',
      message: `Send ${this.montant} TND to ${this.recipient!.displayName}? Total debited: ${this.totalDebite} TND (includes ${this.estimatedFee} TND fee).`,
      confirmText: 'Send',
      cancelText: 'Cancel',
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
      next: (result) => { this.loading = false; this.lastCompletedTransfer = result; this.step = 'success'; this.loadHistory(); },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Transfer failed.'; this.step = 'lookup'; }
    });
  }
 
  resetForm() {
    this.step = 'lookup';
    this.recipient = null;
    this.resolveError = '';
    this.error = '';
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
}