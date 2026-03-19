import { Component, OnDestroy, OnInit } from '@angular/core';
import { VirementService } from '../../../Services/virement.service';
import { Virement } from '../../../Entities/Interfaces/virement';
import { CommonModule } from '@angular/common';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-virement-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './virement-management.html',
  styleUrl: './virement-management.css'
})
export class VirementManagement implements OnInit, OnDestroy {

  private pollSub?: Subscription;

  virements: Virement[] = [];
  loading = false;

  constructor(private adminVirementService: VirementService) { }

  ngOnInit() {
    this.startPolling();
    this.loadVirements();
  }

  loadVirements() {
    this.loading = true;

    this.adminVirementService.getAllVirements().subscribe({
      next: (data) => {
        this.virements = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  startPolling() {
    this.pollSub = interval(5000).subscribe(() => {
      this.loadVirements();
    });
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

}