import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../../Services/auth-service';
import { Treasury } from '../../../Entities/Interfaces/treasury';
import { CommonModule } from '@angular/common';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-treasury-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './treasury-component.html',
  styleUrl: './treasury-component.css',
})
export class TreasuryComponent implements OnInit, OnDestroy {

  treasury: Treasury | null = null;
  loading = true;
  error = '';

  private pollSub?: Subscription;

  constructor(private authService: AuthService) { }

  ngOnInit() {
    this.loadTreasury();
    this.pollSub = interval(5000).subscribe(() => this.loadTreasury());
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  loadTreasury() {
    this.authService.getTreasury().subscribe({
      next: (data) => { this.treasury = data; this.loading = false; },
      error: () => { this.error = 'Failed to load treasury data.'; this.loading = false; }
    });
  }
}