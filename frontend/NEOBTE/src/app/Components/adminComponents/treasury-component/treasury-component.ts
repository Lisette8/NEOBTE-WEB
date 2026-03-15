import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../Services/auth-service';
import { Treasury } from '../../../Entities/Interfaces/treasury';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-treasury-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './treasury-component.html',
  styleUrl: './treasury-component.css',
})
export class TreasuryComponent implements OnInit {
 
  treasury: Treasury | null = null;
  loading = true;
  error = '';
 
  constructor(private authService: AuthService) {}
 
  ngOnInit() {
    this.authService.getTreasury().subscribe({
      next: (data) => { this.treasury = data; this.loading = false; },
      error: () => { this.error = 'Failed to load treasury data.'; this.loading = false; }
    });
  }
}
