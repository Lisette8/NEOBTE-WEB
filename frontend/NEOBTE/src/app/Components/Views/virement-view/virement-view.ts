import { Component, OnInit } from '@angular/core';
import { VirementService } from '../../../Services/virement.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../../../Services/translation-service';

@Component({
  selector: 'app-virement-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './virement-view.html',
  styleUrl: './virement-view.css',
})


export class VirementView implements OnInit {

  virementForm: FormGroup;
  history: any[] = [];
  loading = false;
  message = '';
  error = '';

  constructor(
    private virementService: VirementService,
    private fb: FormBuilder,
    public transService: TranslationService
  ) {
    this.virementForm = this.fb.group({
      compteSourceId: [null, [Validators.required]],
      compteDestinationId: [null, [Validators.required]],
      montant: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit() {
  }



  transfer() {
    if (this.virementForm.invalid) {
      this.virementForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.message = '';
    this.error = '';

    this.virementService.transfer(this.virementForm.value).subscribe({
      next: (res) => {
        this.loading = false;
        this.message = this.transService.translate('virement.success');
        this.virementForm.reset();
        console.log(res);
      },
      error: (err) => {
        this.loading = false;
        this.error = this.transService.translate('virement.error');
        console.error(err);
      }
    });
  }

  
  loadHistory() {
    const sourceId = this.virementForm.get('compteSourceId')?.value;
    if (!sourceId) {
      this.error = "Veuillez saisir un compte source pour voir l'historique";
      return;
    }

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
}
