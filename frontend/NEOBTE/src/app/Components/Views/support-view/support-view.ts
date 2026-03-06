import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SupportService } from '../../../Services/support-service';
import { SupportCreateDTO } from '../../../Entities/DTO/support-create-dto';
import { Support } from '../../../Entities/Interfaces/support';

@Component({
  selector: 'app-support-view',
  standalone: true,
  imports: [CommonModule, FormsModule ],
  templateUrl: './support-view.html',
  styleUrl: './support-view.css',
})


export class SupportView implements OnInit {

  tickets: Support[] = [];

  newTicket: SupportCreateDTO = {
    sujet: '',
    message: ''
  };

  constructor(private supportService: SupportService) {}

  ngOnInit(): void {
    this.loadTickets();
  }


  loadTickets() {
    this.supportService.getMyTickets().subscribe(data => {
      this.tickets = data;
    });
  }
  
  
  createTicket() {

    console.log("button clicked");

    if (!this.newTicket.sujet || !this.newTicket.message) return;

    this.supportService.createTicket(this.newTicket).subscribe(() => {

      this.newTicket = {
        sujet: '',
        message: ''
      };

      this.loadTickets();
    });
  }



}
