import { Component, OnInit } from '@angular/core';
import { Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-compte-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compte-view.html',
  styleUrl: './compte-view.css',
})
export class CompteView implements OnInit {
  
  comptes: Compte[] = [];

  constructor(
    private compteService: CompteService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user.id;
    this.compteService.getUserAccounts(userId).subscribe(data => {
      this.comptes = data;
    });
  }

  goToTransfer(compteId: number) {
    this.router.navigate(['/virement-view'], { state: { compteId } });
  }
}