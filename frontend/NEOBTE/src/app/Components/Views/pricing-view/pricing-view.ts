import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pricing-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pricing-view.html',
  styleUrl: './pricing-view.css',
})
export class PricingView {
  showAgencyModal = false;

  constructor(private router: Router) {}

  openAgencyModal() {
    this.showAgencyModal = true;
  }

  closeAgencyModal() {
    this.showAgencyModal = false;
  }

  goBack() {
    this.router.navigate(['/home-view']);
  }
}
