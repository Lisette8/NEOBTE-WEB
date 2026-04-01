import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClientInsightsData } from '../../Entities/Interfaces/client-premium';
import { NeoChart, NeoChartDataset } from '../neo-chart/neo-chart';

@Component({
  selector: 'app-client-insights',
  standalone: true,
  imports: [CommonModule, RouterLink, NeoChart],
  templateUrl: './client-insights.html',
  styleUrl: './client-insights.css',
})
export class ClientInsights implements OnChanges {
  @Input() isPremium = false;
  @Input() insights: ClientInsightsData | null = null;
  @Input() loading = false;
  @Input() error = '';

  /** Pre-built dataset for the bar chart — stable reference to avoid change-detection issues */
  transferDatasets: NeoChartDataset[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['insights'] && this.insights) {
      this.transferDatasets = [
        { label: 'Envoyé',  data: this.insights.monthlyTransfers.sent },
        { label: 'Reçu',    data: this.insights.monthlyTransfers.received },
      ];
    }
  }
}
