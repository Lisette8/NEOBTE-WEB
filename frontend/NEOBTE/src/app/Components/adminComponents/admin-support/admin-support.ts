import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Support } from '../../../Entities/Interfaces/support';
import { SupportService } from '../../../Services/support-service';
import { WebsocketService } from '../../../Services/websocket.service';

@Component({
  selector: 'app-admin-support',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-support.html',
  styleUrl: './admin-support.css',
})


export class AdminSupport implements OnInit {

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
      console.log('AdminSupport received new ticket via WebSocket:', ticket);
      // Ensure the ticket isn't already in the list
      const exists = this.tickets.some(t => t.idSupport === ticket.idSupport);
      if (!exists) {
        // Create a new array reference to trigger Angular change detection
        this.tickets = [ticket, ...this.tickets];
      }
    });

  }

  loadTickets() {
    this.supportService.getAllTickets().subscribe(data => {
      // Sort by date descending (newest first)
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
