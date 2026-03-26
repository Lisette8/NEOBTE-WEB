import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FraudeConfig } from '../../../Entities/Interfaces/fraude';
import { FraudeService } from '../../../Security/Services/FraudeService';

type PolicyTab = 'courant' | 'epargne' | 'professionnel' | 'fraud';

@Component({
  selector: 'app-account-policy',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account-policy.html',
  styleUrl: './account-policy.css',
})
export class AccountPolicy implements OnInit {

  tab: PolicyTab = 'courant';
  config: FraudeConfig | null = null;
  form: FraudeConfig | null = null;
  loading = true;
  saving = false;
  success = '';
  error = '';

  constructor(private fraudeService: FraudeService) { }

  ngOnInit() {
    this.fraudeService.getConfig().subscribe({
      next: (cfg) => { this.config = cfg; this.form = { ...cfg }; this.loading = false; },
      error: () => { this.error = 'Impossible de charger la configuration.'; this.loading = false; }
    });
  }

  save() {
    if (!this.form) return;
    this.saving = true; this.success = ''; this.error = '';
    this.fraudeService.updateConfig(this.form).subscribe({
      next: (updated) => {
        this.config = updated; this.form = { ...updated };
        this.success = 'Configuration enregistrée avec succès.';
        this.saving = false;
        setTimeout(() => this.success = '', 3500);
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Échec de la sauvegarde.';
        this.saving = false;
      }
    });
  }

  reset() {
    if (this.config) { this.form = { ...this.config }; this.success = ''; this.error = ''; }
  }

  pct(v: number): string { return (v * 100).toFixed(2); }
  setPct(field: keyof FraudeConfig, val: string) {
    if (this.form) (this.form as any)[field] = parseFloat(val) / 100;
  }
}