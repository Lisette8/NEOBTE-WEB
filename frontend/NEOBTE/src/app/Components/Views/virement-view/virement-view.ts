import { Component, OnDestroy, OnInit } from '@angular/core';
import { VirementService } from '../../../Services/virement.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Virement } from '../../../Entities/Interfaces/virement';
import { ACCOUNT_TYPE_META, Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { debounceTime, distinctUntilChanged, interval, Subject, Subscription, switchMap } from 'rxjs';
import { RecipientPreview } from '../../../Entities/Interfaces/recipient-preview';
import { TransferConstraints } from '../../../Entities/Interfaces/transfer-constraints';
import { ActivatedRoute, Router } from '@angular/router';
import { ClientProfile } from '../../../Entities/Interfaces/client-profile';
import { AuthService } from '../../../Services/auth-service';
import { ContratVirementService } from '../../../Services/contrat-virement.service';

export type ErrorKind = 'limit' | 'balance' | 'account' | 'network' | 'generic';

export interface TransferError {
  message: string;
  kind: ErrorKind;
  hint?: string;
  action?: { label: string; route: string };
}

@Component({
  selector: 'app-virement-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './virement-view.html',
  styleUrl: './virement-view.css',
})
export class VirementView implements OnInit, OnDestroy {

  step: 'lookup' | 'confirm' | 'success' = 'lookup';
  mode: 'externe' | 'interne' = 'externe';
  transferForm: FormGroup;
  internalForm: FormGroup;
  recipient: RecipientPreview | null = null;
  resolving = false;
  resolveError = '';
  lastCompletedTransfer: Virement | null = null;
  currentProfile: ClientProfile | null = null;

  private identifierChange$ = new Subject<string>();
  private pollSub?: Subscription;

  loading = false;
  transferError: TransferError | null = null;
  limitWarning = '';
  history: Virement[] = [];
  historyLoading = false;

  comptes: Compte[] = [];
  comptesLoading = false;
  constraintsExternal: TransferConstraints | null = null;
  constraintsInternal: TransferConstraints | null = null;

  readonly accountTypeMeta = ACCOUNT_TYPE_META;

  constructor(
    private virementService: VirementService,
    private compteService: CompteService,
    private fb: FormBuilder,
    private modalService: ConfirmModalService,
    private router: Router,
    private route: ActivatedRoute,
    private contratService: ContratVirementService,
    private authService: AuthService,
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
    this.authService.getCurrentUser().subscribe({ next: (p) => this.currentProfile = p, error: () => { } });
    this.pollSub = interval(50000).subscribe(() => { this.loadHistory(); this.loadComptes(); });

    this.identifierChange$.pipe(
      debounceTime(600),
      distinctUntilChanged(),
      switchMap(identifier => {
        if (!identifier || identifier.length < 5) {
          this.recipient = null; this.resolveError = ''; this.resolving = false; return [];
        }
        this.resolving = true; this.resolveError = '';
        return this.virementService.resolveRecipient(identifier);
      })
    ).subscribe({
      next: (preview) => {
        this.resolving = false;
        if (preview.found) {
          this.recipient = preview; this.resolveError = ''; this.checkLimitWarning();
        } else {
          this.recipient = null;
          this.resolveError = 'Aucun compte trouvé avec cet e-mail ou ce numéro de téléphone.';
        }
      },
      error: () => {
        this.resolving = false; this.recipient = null;
        this.resolveError = 'Impossible de rechercher le destinataire. Vérifiez votre connexion.';
      }
    });

    // Support ?mode=interne query param (e.g. from savings account quick action)
    this.route.queryParams.subscribe(params => {
      if (params['mode'] === 'interne' && this.comptes.length > 1) {
        this.setMode('interne');
      }
    });

    this.transferForm.get('montant')?.valueChanges.subscribe(() => this.checkLimitWarning());
    this.internalForm.get('montant')?.valueChanges.subscribe(() => this.checkLimitWarning());
    this.internalForm.get('compteSourceId')?.valueChanges.subscribe(() => this.checkLimitWarning());
  }

  setMode(mode: 'externe' | 'interne') {
    this.mode = mode; this.step = 'lookup';
    this.transferError = null; this.resolveError = ''; this.limitWarning = '';
    this.recipient = null; this.transferForm.reset(); this.internalForm.reset();
  }

  private loadComptes() {
    this.comptesLoading = true;
    this.compteService.getMyAccounts().subscribe({
      next: (accounts) => { this.comptes = accounts ?? []; this.comptesLoading = false; },
      error: () => { this.comptes = []; this.comptesLoading = false; },
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
  get compteSourceId(): number | null { return this.internalForm.get('compteSourceId')?.value ?? null; }
  get compteDestinationId(): number | null { return this.internalForm.get('compteDestinationId')?.value ?? null; }
  get selectedSource(): Compte | null {
    return this.compteSourceId ? this.comptes.find(c => c.idCompte === this.compteSourceId) ?? null : null;
  }
  get selectedDestination(): Compte | null {
    return this.compteDestinationId ? this.comptes.find(c => c.idCompte === this.compteDestinationId) ?? null : null;
  }

  private get currentConstraints(): TransferConstraints | null {
    if (this.mode === 'interne') {
      if (this.selectedSource) {
        const meta = this.accountTypeMeta[this.selectedSource.typeCompte];
        if (meta) return {
          feeRate: 0, largeTransferThreshold: meta.maxTransfer,
          dailyAmountLimit: null, dailyCountLimit: null, monthlyCountLimit: null,
          canSendExternal: meta.canSendExternal,
          accountTypePurpose: meta.purpose, accountTypeLabel: meta.label,
        };
      }
      return this.constraintsInternal;
    }
    if (this.recipient) return {
      feeRate: this.recipient.feeRate ?? 0,
      largeTransferThreshold: this.recipient.largeTransferThreshold ?? null,
      dailyAmountLimit: this.recipient.dailyAmountLimit ?? null,
      dailyCountLimit: this.recipient.dailyCountLimit ?? null,
      monthlyCountLimit: this.recipient.monthlyCountLimit ?? null,
      canSendExternal: this.recipient.canSendExternal ?? true,
      accountTypePurpose: null, accountTypeLabel: null,
    };
    return this.constraintsExternal;
  }

  get estimatedFee(): number {
    const c = this.currentConstraints;
    if (!c || !this.montant) return 0;
    return Math.round(this.montant * (c.feeRate ?? 0) * 1000) / 1000;
  }
  get totalDebite(): number { return this.montant + this.estimatedFee; }

  get senderCannotSendExternal(): boolean {
    return this.mode !== 'interne' && this.currentConstraints?.canSendExternal === false;
  }

  mediaUrl(url?: string | null): string {
    if (!url) return '';
    return url.startsWith('http') ? url : `http://localhost:8080${url}`;
  }

  accountTypeLabel(type: string): string { return this.accountTypeMeta[type]?.label ?? type; }
  accountTypeIconClass(type: string): string {
    return this.accountTypeMeta[type]?.icon ?? 'fa-solid fa-building-columns';
  }
  accountTypeColor(type: string): string { return this.accountTypeMeta[type]?.color ?? '#6b7280'; }

  // ── Limit warning (soft, inline, before submit) ─────────────────────────

  checkLimitWarning() {
    this.limitWarning = '';
    const c = this.currentConstraints;
    if (!c || !this.montant) return;
    if (c.largeTransferThreshold && this.montant > c.largeTransferThreshold) {
      this.limitWarning = `Montant dépasse la limite de ${this.fmt(c.largeTransferThreshold)} TND par virement.`;
      return;
    }
    if (c.dailyAmountLimit && this.montant > c.dailyAmountLimit) {
      this.limitWarning = `Montant dépasse la limite journalière de ${this.fmt(c.dailyAmountLimit)} TND.`;
    }
  }

  get hasLimitError(): boolean {
    const c = this.currentConstraints;
    return !!(c?.largeTransferThreshold && this.montant > c.largeTransferThreshold);
  }

  dismissError() { this.transferError = null; }

  // ── Error classification ────────────────────────────────────────────────

  private classifyError(raw: string): TransferError {
    const msg = raw || '';

    if (/solde insuffisant/i.test(msg) || /solde disponible/i.test(msg) || /solde actuel/i.test(msg)) {
      return {
        message: msg,
        kind: 'balance',
        hint: 'Vérifiez votre solde avant de réessayer. Les frais de service sont inclus dans le total débité.',
        action: { label: 'Voir mes comptes', route: '/compte-view' }
      };
    }

    if (/limite journalière|limite mensuelle|nombre de virements|par jour|par mois/i.test(msg)) {
      return {
        message: msg,
        kind: 'limit',
        hint: 'Ces limites sont définies par votre type de compte. Vous pouvez effectuer un transfert interne vers un compte chèque pour contourner les restrictions d\'épargne.',
      };
    }

    if (/seuil autorisé|limite par virement|montant trop élevé/i.test(msg)) {
      return {
        message: msg,
        kind: 'limit',
        hint: 'Pour des virements plus importants, contactez le support BTE.',
        action: { label: 'Contacter le support', route: '/support-view' }
      };
    }

    if (/compte épargne|virements externes non disponibles/i.test(msg)) {
      return {
        message: msg,
        kind: 'account',
        hint: 'Utilisez d\'abord le transfert interne pour déplacer les fonds vers votre compte chèque.',
        action: { label: 'Transfert interne', route: '/virement-view' }
      };
    }

    if (/n'est pas actif|suspendu|bloqué/i.test(msg)) {
      return {
        message: msg,
        kind: 'account',
        hint: 'Réactivez votre compte depuis la page de détail pour pouvoir effectuer des virements.',
        action: { label: 'Gérer mes comptes', route: '/compte-view' }
      };
    }

    if (/connexion|réseau|timeout|0 unknown/i.test(msg) || msg === '') {
      return {
        message: 'Impossible de contacter le serveur. Vérifiez votre connexion internet.',
        kind: 'network',
        hint: 'Si le problème persiste, réessayez dans quelques instants.',
      };
    }

    return { message: msg || 'Une erreur inattendue s\'est produite.', kind: 'generic' };
  }

  private setError(err: any) {
    const raw = err?.error?.message || err?.message || '';
    this.transferError = this.classifyError(raw);
  }

  // ── Flow ────────────────────────────────────────────────────────────────

  proceedToConfirm() {
    this.transferError = null;
    if (this.mode === 'externe') {
      if (this.senderCannotSendExternal) {
        this.transferError = this.classifyError(
          'Un compte épargne ne peut pas effectuer de virements externes.');
        return;
      }
      if (!this.recipient) { this.resolveError = 'Veuillez saisir un destinataire valide.'; return; }
      if (!this.montant || this.transferForm.get('montant')?.invalid) {
        this.transferForm.get('montant')?.markAsTouched(); return;
      }
    } else {
      if (this.internalForm.invalid) {
        Object.values(this.internalForm.controls).forEach(c => c.markAsTouched()); return;
      }
      if (this.compteSourceId && this.compteDestinationId && this.compteSourceId === this.compteDestinationId) {
        this.transferError = { message: 'Veuillez sélectionner deux comptes différents.', kind: 'generic' };
        return;
      }
    }
    if (this.hasLimitError) {
      this.transferError = this.classifyError(this.limitWarning); return;
    }
    this.step = 'confirm';
  }

  async confirmTransfer() {
    if (this.mode === 'interne') {
      const srcLabel = this.selectedSource ? this.accountTypeLabel(this.selectedSource.typeCompte) : `#${this.compteSourceId}`;
      const dstLabel = this.selectedDestination ? this.accountTypeLabel(this.selectedDestination.typeCompte) : `#${this.compteDestinationId}`;
      const confirmed = await this.modalService.confirm({
        title: 'Confirmer le transfert interne',
        message: `Transférer ${this.fmt(this.montant)} TND du ${srcLabel} (#${this.compteSourceId}) vers le ${dstLabel} (#${this.compteDestinationId}) ?`,
        confirmText: 'Transférer', cancelText: 'Annuler', type: 'warning'
      });
      if (!confirmed) return;

      this.loading = true; this.transferError = null;
      this.virementService.transferInterne({
        compteSourceId: this.compteSourceId!,
        compteDestinationId: this.compteDestinationId!,
        montant: this.montant,
        idempotencyKey: crypto.randomUUID(),
      }).subscribe({
        next: (result) => {
          this.loading = false; this.lastCompletedTransfer = result;
          this.step = 'success'; this.loadHistory(); this.loadComptes();
        },
        error: (err) => {
          this.loading = false; this.setError(err); this.step = 'confirm';
        }
      });
      return;
    }

    const confirmed = await this.modalService.confirm({
      title: 'Confirmer le virement',
      message: `Envoyer ${this.fmt(this.montant)} TND à ${this.recipient!.displayName} ? Total débité : ${this.fmt(this.totalDebite)} TND (dont ${this.fmt(this.estimatedFee)} TND de frais).`,
      confirmText: 'Envoyer', cancelText: 'Annuler', type: 'warning'
    });
    if (!confirmed) return;

    this.loading = true; this.transferError = null;
    this.virementService.transfer({
      recipientIdentifier: this.transferForm.value.recipientIdentifier,
      montant: this.montant,
      idempotencyKey: crypto.randomUUID(),
    }).subscribe({
      next: (result) => {
        this.loading = false; this.lastCompletedTransfer = result;
        this.step = 'success'; this.loadHistory(); this.loadComptes();
      },
      error: (err) => {
        this.loading = false; this.setError(err); this.step = 'confirm';
      }
    });
  }

  navigateError(route: string) {
    if (route === '/virement-view') { this.setMode('interne'); return; }
    this.router.navigate([route]);
  }

  downloadContrat() {
    if (!this.lastCompletedTransfer) return;
    this.contratService.printVirement(this.lastCompletedTransfer, this.currentProfile, this.mode);
  }

  downloadContratFromHistory(v: Virement) {
    const mode = v.senderName === v.recipientName ? 'interne' : 'externe';
    this.contratService.printVirement(v, this.currentProfile, mode);
  }

  resetForm() {
    this.step = 'lookup'; this.recipient = null;
    this.resolveError = ''; this.transferError = null; this.limitWarning = '';
    this.lastCompletedTransfer = null; this.transferForm.reset();
  }

  loadHistory() {
    this.historyLoading = true;
    this.virementService.getHistory().subscribe({
      next: (data) => { this.history = data; this.historyLoading = false; },
      error: () => { this.historyLoading = false; }
    });
  }

  fmt(n: number): string {
    return n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
  }

  ngOnDestroy(): void { this.pollSub?.unsubscribe(); }
}