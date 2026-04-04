import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SupportService } from '../../../Services/support-service';
import { SupportCreateDTO } from '../../../Entities/DTO/support-create-dto';
import { Support, SupportCategorie, SupportStatus } from '../../../Entities/Interfaces/support';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-support-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './support-view.html',
  styleUrl: './support-view.css',
})
export class SupportView implements OnInit, OnDestroy {

  tickets: Support[] = [];
  loading = false;
  submitting = false;
  error = '';
  successMessage = '';

  expandedIds = new Set<number>();
  activeFilter: SupportStatus | 'ALL' = 'ALL';

  newTicket: SupportCreateDTO = {
    sujet: '',
    message: '',
    categorie: 'AUTRE',
    priorite: 'NORMALE',
  };

  private pollSub?: Subscription;

  // ── Category metadata ────────────────────────────────────────────────────
  readonly categories: { value: SupportCategorie; label: string; icon: string; cssClass: string }[] = [
    { value: 'VIREMENT', label: 'Virement', icon: 'fa-solid fa-right-left', cssClass: 'purple' },
    { value: 'COMPTE', label: 'Compte', icon: 'fa-solid fa-credit-card', cssClass: 'blue' },
    { value: 'CARTE', label: 'Carte', icon: 'fa-solid fa-id-card', cssClass: 'navy' },
    { value: 'PRET', label: 'Prêt', icon: 'fa-solid fa-hand-holding-dollar', cssClass: 'amber' },
    { value: 'PLACEMENT', label: 'Placement', icon: 'fa-solid fa-chart-line', cssClass: 'green' },
    { value: 'SECURITE', label: 'Sécurité', icon: 'fa-solid fa-shield-halved', cssClass: 'red' },
    { value: 'AUTRE', label: 'Autre', icon: 'fa-solid fa-circle-question', cssClass: 'grey' },
  ];

  readonly statusFilters: { value: SupportStatus | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'Tous' },
    { value: 'OPEN', label: 'Ouverts' },
    { value: 'IN_PROGRESS', label: 'En cours' },
    { value: 'RESOLVED', label: 'Résolus' },
    { value: 'CLOSED', label: 'Clôturés' },
  ];

  constructor(private supportService: SupportService) { }

  ngOnInit(): void {
    this.loadTickets();
    this.pollSub = interval(30000).subscribe(() => this.loadTickets());
  }

  ngOnDestroy(): void { this.pollSub?.unsubscribe(); }

  loadTickets() {
    this.loading = true;
    this.supportService.getMyTickets().subscribe({
      next: (data) => { this.tickets = data; this.loading = false; },
      error: () => { this.error = 'Impossible de charger les tickets.'; this.loading = false; },
    });
  }

  canSubmit(): boolean {
    return !!this.newTicket.sujet.trim() && !!this.newTicket.message.trim();
  }

  createTicket() {
    if (!this.canSubmit()) return;
    this.submitting = true;
    this.successMessage = '';
    this.error = '';
    this.supportService.createTicket(this.newTicket).subscribe({
      next: () => {
        this.newTicket = { sujet: '', message: '', categorie: 'AUTRE', priorite: 'NORMALE' };
        this.successMessage = 'Ticket envoyé ! Notre équipe vous répondra sous 24–72h.';
        this.submitting = false;
        this.loadTickets();
        setTimeout(() => this.successMessage = '', 6000);
      },
      error: () => { this.error = "Échec de l'envoi du ticket."; this.submitting = false; },
    });
  }

  get filteredTickets(): Support[] {
    if (this.activeFilter === 'ALL') return this.tickets;
    return this.tickets.filter(t => t.status === this.activeFilter);
  }

  toggleExpand(id: number) {
    this.expandedIds.has(id) ? this.expandedIds.delete(id) : this.expandedIds.add(id);
  }

  // ── Status helpers ───────────────────────────────────────────────────────

  getStatusLabel(status: SupportStatus): string {
    const map: Record<SupportStatus, string> = {
      OPEN: 'Ouvert', IN_PROGRESS: 'En cours', RESOLVED: 'Résolu', CLOSED: 'Clôturé',
    };
    return map[status] ?? status;
  }

  getStatusClass(status: SupportStatus): string {
    const map: Record<SupportStatus, string> = {
      OPEN: 'open', IN_PROGRESS: 'progress', RESOLVED: 'resolved', CLOSED: 'closed',
    };
    return map[status] ?? 'open';
  }

  getStatusIcon(status: SupportStatus): string {
    const map: Record<SupportStatus, string> = {
      OPEN: 'fa-solid fa-circle-dot',
      IN_PROGRESS: 'fa-solid fa-circle-half-stroke',
      RESOLVED: 'fa-solid fa-circle-check',
      CLOSED: 'fa-solid fa-circle-xmark',
    };
    return map[status] ?? 'fa-regular fa-circle';
  }

  isStatusReached(current: SupportStatus, target: SupportStatus): boolean {
    const order: SupportStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
    return order.indexOf(current) >= order.indexOf(target);
  }

  // ── Category helpers ─────────────────────────────────────────────────────

  getCatLabel(cat: SupportCategorie): string {
    return this.categories.find(c => c.value === cat)?.label ?? cat;
  }

  getCatIcon(cat: SupportCategorie): string {
    return this.categories.find(c => c.value === cat)?.icon ?? 'fa-solid fa-circle-question';
  }

  getCatClass(cat: SupportCategorie): string {
    return this.categories.find(c => c.value === cat)?.cssClass ?? 'grey';
  }
}
