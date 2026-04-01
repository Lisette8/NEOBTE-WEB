import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RevealOnScrollDirective } from '../../../Directives/reveal-on-scroll.directive';

@Component({
  selector: 'app-pricing-view',
  standalone: true,
  imports: [CommonModule, RevealOnScrollDirective],
  templateUrl: './pricing-view.html',
  styleUrl: './pricing-view.css',
})
export class PricingView {
  showAgencyModal = false;

  openAgencyModal() {
    this.showAgencyModal = true;
  }

  closeAgencyModal() {
    this.showAgencyModal = false;
  }
}
