import { Component, OnDestroy, OnInit } from '@angular/core';
import { VirementService } from '../../../Services/virement.service';
import { Virement } from '../../../Entities/Interfaces/virement';
import { CommonModule } from '@angular/common';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';

@Component({
  selector: 'app-virement-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './virement-management.html',
  styleUrl: './virement-management.css'
})
export class VirementManagement implements OnInit, OnDestroy {

  virements: Virement[] = [];
  loading = false;

  constructor(
    private adminVirementService: VirementService,
    private ws: WebsocketService
  ) { }

  ngOnInit() {
    this.loadVirements();
    this.ws.subscribeAdmin((event) => {
      if (event.type === 'VIREMENT') this.loadVirements();
    });
  }

  ngOnDestroy() {
    // WebsocketService is a singleton — do NOT disconnect here,
    // other components share the same connection.
  }

  loadVirements() {
    this.loading = true;
    this.adminVirementService.getAllVirements().subscribe({
      next: (data) => { this.virements = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}