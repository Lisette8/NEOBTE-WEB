import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SupportService } from '../../../Services/support-service';
import { SupportCreateDTO } from '../../../Entities/DTO/support-create-dto';
import { Support } from '../../../Entities/Interfaces/support';

@Component({
  selector: 'app-support-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './support-view.html',
  styleUrl: './support-view.css',
})
export class SupportView implements OnInit {

  tickets: Support[] = [];
  loading = false;
  submitting = false;
  error = '';
  successMessage = '';

  newTicket: SupportCreateDTO = {
    sujet: '',
    message: ''
  };

  constructor(private supportService: SupportService) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets() {
    this.loading = true;
    this.supportService.getMyTickets().subscribe({
      next: (data) => {
        this.tickets = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load tickets.';
        this.loading = false;
      }
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
        this.successMessage = 'Ticket submitted successfully!';
        this.submitting = false;
        this.loadTickets();
      },
      error: () => {
        this.error = 'Failed to submit ticket.';
        this.submitting = false;
      }
    });
  }
}
