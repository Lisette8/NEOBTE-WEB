import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SupportService } from '../../../Services/support-service';
import { SupportCreateDTO } from '../../../Entities/DTO/support-create-dto';
import { Support } from '../../../Entities/Interfaces/support';
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

  newTicket: SupportCreateDTO = { sujet: '', message: '' };

  private pollSub?: Subscription;

  constructor(private supportService: SupportService) { }

  ngOnInit(): void {
    this.loadTickets();
    this.pollSub = interval(2000).subscribe(() => this.loadTickets());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  loadTickets() {
    this.loading = true;
    this.supportService.getMyTickets().subscribe({
      next: (data) => { this.tickets = data; this.loading = false; },
      error: () => { this.error = 'Impossible de charger les tickets.'; this.loading = false; }
    });
  }

  createTicket() {
    if (!this.newTicket.sujet || !this.newTicket.message) return;
    this.submitting = true;
    this.successMessage = '';
    this.error = '';
    this.supportService.createTicket(this.newTicket).subscribe({
      next: () => {
        this.newTicket = { sujet: '', message: '' };
        this.successMessage = 'Ticket envoyé avec succès !';
        this.submitting = false;
        this.loadTickets();
      },
      error: () => { this.error = "Échec de l'envoi du ticket."; this.submitting = false; }
    });
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'OPEN': return 'Ouvert';
      case 'IN_PROGRESS': return 'En cours';
      case 'RESOLVED': return 'Résolu';
      case 'CLOSED': return 'Fermé';
      default: return status;
    }
  }
}