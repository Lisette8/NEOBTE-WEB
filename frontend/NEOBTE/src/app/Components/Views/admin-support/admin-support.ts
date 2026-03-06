import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Support } from '../../../Entities/Interfaces/support';
import { SupportService } from '../../../Services/support-service';

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

  constructor(private supportService: SupportService) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets() {
    this.supportService.getAllTickets().subscribe(data => {
      this.tickets = data;
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
