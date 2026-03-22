import { Component, OnDestroy, OnInit } from '@angular/core';
import { Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { AuthService } from '../../../Services/auth-service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DemandeCompte } from '../../../Entities/Interfaces/demande-compte';
import { DemandeCompteCreateDTO } from '../../../Entities/DTO/demande-compte-create-dto';
import { AccountPhysicalCard } from '../../account-physical-card/account-physical-card';
import { interval, Subscription } from 'rxjs';
import { ClientProfile } from '../../../Entities/Interfaces/client-profile';

@Component({
  selector: 'app-compte-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AccountPhysicalCard],
  templateUrl: './compte-view.html',
  styleUrl: './compte-view.css',
})
export class CompteView implements OnInit, OnDestroy {

  comptes: Compte[] = [];
  demandes: DemandeCompte[] = [];
  profile: ClientProfile | null = null;
  loading = false;
  error = '';

  step: 'list' | 'select-type' | 'fill-kyc' = 'list';
  selectedType: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL' | null = null;
  kycForm!: FormGroup;
  submitting = false;
  submitSuccess = '';
  submitError = '';

  private pollSub?: Subscription;

  readonly accountTypes = [
    {
      type: 'COURANT' as const, label: 'Compte Chèque',
      desc: 'Opérations quotidiennes — dépôts, retraits, virements',
      icon: 'card'
    },
    {
      type: 'EPARGNE' as const, label: 'Compte Épargne',
      desc: 'Faites fructifier votre épargne avec des intérêts',
      icon: 'savings'
    },
    {
      type: 'PROFESSIONNEL' as const, label: 'Compte Professionnel',
      desc: "Banque d'affaires pour professionnels et indépendants",
      icon: 'business'
    }
  ];

  constructor(
    private compteService: CompteService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['open'] === 'true') this.step = 'select-type';
    });
    this.loadData();
    this.pollSub = interval(50000).subscribe(() => this.loadData());
  }

  ngOnDestroy(): void { this.pollSub?.unsubscribe(); }

  loadData() {
    const userId = this.authService.getUserId();
    if (!userId) return;
    this.loading = true;
    this.compteService.getUserAccounts(userId).subscribe({
      next: (data) => { this.comptes = data; this.loading = false; },
      error: () => { this.error = 'Impossible de charger les comptes.'; this.loading = false; }
    });
    this.compteService.getMyDemandes().subscribe({
      next: (data) => this.demandes = data,
      error: () => { }
    });
    // Load profile to pre-fill KYC fields
    this.authService.getCurrentUser().subscribe({
      next: (p) => this.profile = p,
      error: () => { }
    });
  }

  selectType(type: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL') {
    this.selectedType = type;
    this.buildKycForm(type);
    this.step = 'fill-kyc';
  }

  /**
   * Build form with only the fields that are missing from the profile.
   * Known fields are pre-filled and locked (disabled).
   */
  private buildKycForm(type: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL') {
    const p = this.profile;
    const hasCin = !!p?.cin;
    const hasDob = !!p?.dateNaissance;
    const hasAdresse = !!p?.adresse;
    const hasJob = !!p?.job;

    const cinCtrl = hasCin
      ? [{ value: p!.cin, disabled: true }]
      : ['', [Validators.required, Validators.pattern(/^[0-9]{8}$/)]];

    const dobCtrl = hasDob
      ? [{ value: p!.dateNaissance, disabled: true }]
      : ['', Validators.required];

    const base = { cin: cinCtrl, dateNaissance: dobCtrl, motif: [''] };

    if (type === 'COURANT') {
      const adresseCtrl = hasAdresse
        ? [{ value: p!.adresse, disabled: true }]
        : ['', Validators.required];
      const jobCtrl = hasJob
        ? [{ value: p!.job, disabled: true }]
        : ['', Validators.required];
      this.kycForm = this.fb.group({ ...base, adresse: adresseCtrl, job: jobCtrl });

    } else if (type === 'EPARGNE') {
      this.kycForm = this.fb.group(base);

    } else { // PROFESSIONNEL
      const adresseCtrl = hasAdresse
        ? [{ value: p!.adresse, disabled: true }]
        : ['', Validators.required];
      const jobCtrl = hasJob
        ? [{ value: p!.job, disabled: true }]
        : ['', Validators.required];
      this.kycForm = this.fb.group({
        ...base,
        adresse: adresseCtrl,
        job: jobCtrl,
        nomEntreprise: ['', Validators.required],
      });
    }
  }

  submitDemande() {
    if (this.kycForm.invalid) { this.kycForm.markAllAsTouched(); return; }
    this.submitting = true;
    this.submitError = '';

    // getRawValue() includes disabled fields
    const v = this.kycForm.getRawValue();

    const dto: DemandeCompteCreateDTO = {
      typeCompte: this.selectedType!,
      motif: v.motif || undefined,
      // Only send cin/dob if they were editable (not already on profile)
      cin: this.profile?.cin ? undefined : v.cin,
      dateNaissance: this.profile?.dateNaissance ? undefined : v.dateNaissance,
      adresse: this.profile?.adresse ? undefined : v.adresse || undefined,
      job: this.profile?.job ? undefined : v.job || undefined,
      nomEntreprise: v.nomEntreprise || undefined,
    };

    this.compteService.submitDemandeCompte(dto).subscribe({
      next: () => {
        this.submitting = false;
        this.submitSuccess = "Votre demande a été soumise ! Un administrateur l'examinera prochainement.";
        this.step = 'list';
        this.loadData();
      },
      error: (err: any) => {
        this.submitting = false;
        this.submitError = err?.error?.message || 'Échec de la soumission.';
      }
    });
  }

  sanitizeCin(event: Event) {
    const input = event.target as HTMLInputElement;
    const val = input.value.replace(/\D/g, '');
    input.value = val;
    this.kycForm?.get('cin')?.setValue(val, { emitEvent: false });
  }

  fieldInvalid(field: string): boolean {
    const ctrl = this.kycForm?.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  isLocked(field: string): boolean {
    return this.kycForm?.get(field)?.disabled ?? false;
  }

  cancelRequest() { this.step = 'select-type'; this.selectedType = null; this.submitError = ''; }

  goBack() {
    if (this.step === 'fill-kyc') { this.step = 'select-type'; return; }
    this.router.navigate(['/home-view']);
  }

  get pendingDemandes() { return this.demandes.filter(d => d.statutDemande === 'EN_ATTENTE'); }

  get availableAccountTypes() {
    const ownedTypes = this.comptes.map(c => c.typeCompte);
    const pendingTypes = this.demandes.filter(d => d.statutDemande === 'EN_ATTENTE').map(d => d.typeCompte);
    const takenTypes = new Set([...ownedTypes, ...pendingTypes]);
    return this.accountTypes.filter(t => !takenTypes.has(t.type));
  }
}
