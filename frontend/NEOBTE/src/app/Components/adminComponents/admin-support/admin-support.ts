import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Support } from '../../../Entities/Interfaces/support';
import { SupportService } from '../../../Services/support-service';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';

@Component({
  selector: 'app-admin-support',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-support.html',
  styleUrl: './admin-support.css',
})
export class AdminSupport implements OnInit, OnDestroy {

  tickets: Support[] = [];
  responseText: { [key: number]: string } = {};
  statusSelected: { [key: number]: string } = {};

  constructor(
    private supportService: SupportService,
    private websocket: WebsocketService
  ) { }

  ngOnInit() {
    this.loadTickets();

    this.websocket.connect((ticket) => {
      const exists = this.tickets.some(t => t.idSupport === ticket.idSupport);
      if (!exists) {
        this.tickets = [ticket, ...this.tickets];
      }
    });
  }

  // FIX: disconnect WebSocket when component is destroyed to prevent connection leaks
  ngOnDestroy() {
    this.websocket.disconnect();
  }

  loadTickets() {
    this.supportService.getAllTickets().subscribe(data => {
      this.tickets = data.sort((a, b) =>
        new Date(b.dateCreation).getTime() - new Date(a.dateCreation).getTime()
      );
    });
  }

  updateTicket(ticket: Support) {
    const response = this.responseText[ticket.idSupport];
    const status = this.statusSelected[ticket.idSupport] || ticket.status;

    this.supportService
      .updateTicket(ticket.idSupport, response, status)
      .subscribe(() => {
        this.loadTickets();
      });
  }

  deleteTicket(id: number) {
    this.supportService.deleteTicket(id).subscribe(() => {
      this.loadTickets();
    });
  }
}