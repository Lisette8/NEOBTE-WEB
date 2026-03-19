import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FilterStatutPipe } from '../../../Security/Services/FilterStatutPipe';
import { FraudeAlerte, FraudeConfig } from '../../../Entities/Interfaces/fraude';
import { FraudeService } from '../../../Security/Services/FraudeService';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';

type SecurityTab = 'alerts' | 'config';
type AlertFilter = 'ALL' | 'OPEN' | 'REVIEWED' | 'DISMISSED';

@Component({
  selector: 'app-fraude-management',
  standalone: true,
  imports: [CommonModule, FormsModule, FilterStatutPipe],
  templateUrl: './fraude-management.html',
  styleUrl: './fraude-management.css',
})
export class FraudeManagement implements OnInit, OnDestroy {

  secTab: SecurityTab = 'alerts';
  alertFilter: AlertFilter = 'OPEN';
  alertes: FraudeAlerte[] = [];
  alertesLoading = true;
  alertesError = '';
  openCount = 0;

  reviewingId: number | null = null;
  reviewNote = '';
  reviewSubmitting = false;
  reviewError = '';

  config: FraudeConfig | null = null;
  configForm: FraudeConfig | null = null;
  configLoading = true;
  configSaving = false;
  configSuccess = '';
  configError = '';

  constructor(private fraudeService: FraudeService, private ws: WebsocketService) { }

  ngOnInit() {
    this.loadConfig();
    this.loadAlertes();
    // Real-time: reload when new fraud alerts arrive
    this.ws.subscribeAdmin((event) => {
      if (event.type === 'FRAUDE') this.loadAlertes();
    });
  }

  ngOnDestroy() { }

  loadAlertes() {
    this.fraudeService.getAllAlertes().subscribe({
      next: (data) => {
        this.alertes = data;
        this.openCount = data.filter(a => a.statut === 'OPEN').length;
        this.alertesLoading = false;
      },
      error: () => { this.alertesLoading = false; this.alertesError = 'Failed to load alerts.'; }
    });
  }

  get filteredAlertes(): FraudeAlerte[] {
    if (this.alertFilter === 'ALL') return this.alertes;
    return this.alertes.filter(a => a.statut === this.alertFilter);
  }

  openReview(id: number) { this.reviewingId = id; this.reviewNote = ''; this.reviewError = ''; }
  cancelReview() { this.reviewingId = null; this.reviewNote = ''; this.reviewError = ''; }

  submitReview(newStatut: 'REVIEWED' | 'DISMISSED') {
    if (this.reviewingId === null) return;
    this.reviewSubmitting = true;
    this.reviewError = '';
    this.fraudeService.reviewAlerte(this.reviewingId, newStatut, this.reviewNote).subscribe({
      next: (updated) => {
        const idx = this.alertes.findIndex(a => a.id === updated.id);
        if (idx > -1) this.alertes[idx] = updated;
        this.openCount = this.alertes.filter(a => a.statut === 'OPEN').length;
        this.reviewSubmitting = false;
        this.reviewingId = null;
      },
      error: (err: any) => {
        this.reviewError = err?.error?.message || 'Failed to update alert.';
        this.reviewSubmitting = false;
      }
    });
  }

  loadConfig() {
    this.configLoading = true;
    this.fraudeService.getConfig().subscribe({
      next: (cfg) => { this.config = cfg; this.configForm = { ...cfg }; this.configLoading = false; },
      error: () => { this.configLoading = false; this.configError = 'Failed to load config.'; }
    });
  }

  saveConfig() {
    if (!this.configForm) return;
    this.configSaving = true;
    this.configSuccess = ''; this.configError = '';
    this.fraudeService.updateConfig(this.configForm).subscribe({
      next: (updated) => {
        this.config = updated; this.configForm = { ...updated };
        this.configSuccess = 'Configuration saved.';
        this.configSaving = false;
        setTimeout(() => this.configSuccess = '', 3000);
      },
      error: (err: any) => { this.configError = err?.error?.message || 'Failed to save config.'; this.configSaving = false; }
    });
  }

  resetConfigForm() { if (this.config) this.configForm = { ...this.config }; this.configSuccess = ''; this.configError = ''; }

  severityClass(s: string): string {
    switch (s) { case 'HIGH': return 'sev-high'; case 'MEDIUM': return 'sev-medium'; case 'LOW': return 'sev-low'; default: return ''; }
  }

  statutClass(s: string): string {
    switch (s) { case 'OPEN': return 'st-open'; case 'REVIEWED': return 'st-reviewed'; case 'DISMISSED': return 'st-dismissed'; default: return ''; }
  }

  typeLabel(t: string): string {
    switch (t) {
      case 'SUSPICIOUS_HOUR': return 'Suspicious Hour'; case 'DAILY_COUNT_EXCEEDED': return 'Daily Count Exceeded';
      case 'DAILY_AMOUNT_EXCEEDED': return 'Daily Amount Exceeded'; case 'RAPID_SUCCESSION': return 'Rapid Succession';
      case 'LARGE_SINGLE_TRANSFER': return 'Large Transfer'; default: return t;
    }
  }

  typeIcon(t: string): string {
    switch (t) {
      case 'SUSPICIOUS_HOUR': return 'clock'; case 'DAILY_COUNT_EXCEEDED': return 'repeat';
      case 'DAILY_AMOUNT_EXCEEDED': return 'trending-up'; case 'RAPID_SUCCESSION': return 'zap';
      case 'LARGE_SINGLE_TRANSFER': return 'alert-triangle'; default: return 'shield';
    }
  }
}