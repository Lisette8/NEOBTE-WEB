import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-pricing-inapp-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './pricing-inapp-view.html',
  styleUrl: './pricing-inapp-view.css',
})
export class PricingInappView {
  showAgencyModal = false;

  openAgencyModal() {
    this.showAgencyModal = true;
  }

  closeAgencyModal() {
    this.showAgencyModal = false;
  }
}

