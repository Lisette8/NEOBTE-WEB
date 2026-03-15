import { Component, OnInit } from '@angular/core';
import { Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { AuthService } from '../../../Services/auth-service';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DemandeCompte } from '../../../Entities/Interfaces/demande-compte';
import { DemandeCompteCreateDTO } from '../../../Entities/DTO/demande-compte-create-dto';

@Component({
  selector: 'app-compte-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './compte-view.html',
  styleUrl: './compte-view.css',
})
export class CompteView implements OnInit {
 
  comptes: Compte[] = [];
  demandes: DemandeCompte[] = [];
  loading = false;
  error = '';
 
  step: 'list' | 'select-type' | 'fill-kyc' = 'list';
  selectedType: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL' | null = null;
  kycForm!: FormGroup;
  submitting = false;
  submitSuccess = '';
  submitError = '';
 
  readonly accountTypes = [
    {
      type: 'COURANT' as const,
      label: 'Compte Chèque',
      icon: '🏦',
      desc: 'Everyday banking — deposits, withdrawals, transfers',
      needs: 'CIN · Date of birth · Address · Profession'
    },
    {
      type: 'EPARGNE' as const,
      label: 'Compte Épargne',
      icon: '💰',
      desc: 'Earn interest on your savings',
      needs: 'CIN · Date of birth'
    },
    {
      type: 'PROFESSIONNEL' as const,
      label: 'Compte Professionnel',
      icon: '💼',
      desc: 'Business banking for professionals and freelancers',
      needs: 'CIN · Date of birth · Address · Profession · Company name'
    }
  ];
 
  constructor(
    private compteService: CompteService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
  ) {}
 
  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['open'] === 'true') {
        this.step = 'select-type';
      }
    });
    this.loadData();
  }
 
  loadData() {
    const userId = this.authService.getUserId();
    if (!userId) return;
 
    this.loading = true;
    this.compteService.getUserAccounts(userId).subscribe({
      next: (data) => { this.comptes = data; this.loading = false; },
      error: () => { this.error = 'Failed to load accounts.'; this.loading = false; }
    });
 
    this.compteService.getMyDemandes().subscribe({
      next: (data) => this.demandes = data,
      error: () => {}
    });
  }
 
  selectType(type: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL') {
    this.selectedType = type;
    this.buildKycForm(type);
    this.step = 'fill-kyc';
  }
 
  private buildKycForm(type: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL') {
    const base = {
      cin:           ['', [Validators.required, Validators.pattern(/^[0-9]{8}$/)]],
      dateNaissance: ['', Validators.required],
      motif:         [''],
    };
 
    if (type === 'COURANT') {
      this.kycForm = this.fb.group({ ...base, adresse: ['', Validators.required], job: ['', Validators.required] });
    } else if (type === 'EPARGNE') {
      this.kycForm = this.fb.group(base);
    } else {
      this.kycForm = this.fb.group({ ...base, adresse: ['', Validators.required], job: ['', Validators.required], nomEntreprise: ['', Validators.required] });
    }
  }
 
  submitDemande() {
    if (this.kycForm.invalid) { this.kycForm.markAllAsTouched(); return; }
 
    this.submitting = true;
    this.submitError = '';
 
    const v = this.kycForm.value;
    const dto: DemandeCompteCreateDTO = {
      typeCompte: this.selectedType!,
      cin: v.cin,
      dateNaissance: v.dateNaissance,
      motif: v.motif || undefined,
      adresse: v.adresse || undefined,
      job: v.job || undefined,
      nomEntreprise: v.nomEntreprise || undefined,
    };
 
    this.compteService.submitDemandeCompte(dto).subscribe({
      next: () => {
        this.submitting = false;
        this.submitSuccess = 'Your request has been submitted! An admin will review it shortly.';
        this.step = 'list';
        this.loadData();
      },
      error: (err) => {
        this.submitting = false;
        this.submitError = err?.error?.message || 'Failed to submit request.';
      }
    });
  }
 
  fieldInvalid(field: string): boolean {
    const ctrl = this.kycForm?.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
 
  cancelRequest() {
    this.step = 'select-type';
    this.selectedType = null;
    this.submitError = '';
  }
 
  goBack() {
    if (this.step === 'fill-kyc') { this.step = 'select-type'; return; }
    this.router.navigate(['/home-view']);
  }
 
  get pendingDemandes() {
    return this.demandes.filter(d => d.statutDemande === 'EN_ATTENTE');
  }
 
  get availableAccountTypes() {
    const ownedTypes = this.comptes.map(c => c.typeCompte);
    const pendingTypes = this.demandes
      .filter(d => d.statutDemande === 'EN_ATTENTE')
      .map(d => d.typeCompte);
    const takenTypes = new Set([...ownedTypes, ...pendingTypes]);
    return this.accountTypes.filter(t => !takenTypes.has(t.type));
  }
}