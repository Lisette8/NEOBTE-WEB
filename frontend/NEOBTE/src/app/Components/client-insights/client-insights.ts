import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  BarController,
  CategoryScale,
  Chart,
  ChartConfiguration,
  Filler,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
  BarElement,
} from 'chart.js';
import { RouterLink } from '@angular/router';
import { ClientInsightsData } from '../../Entities/Interfaces/client-premium';

Chart.register(
  LineController,
  BarController,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Tooltip,
  Legend,
  Filler
);

@Component({
  selector: 'app-client-insights',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './client-insights.html',
  styleUrl: './client-insights.css',
})
export class ClientInsights implements AfterViewInit, OnChanges, OnDestroy {
  @Input() isPremium = false;
  @Input() insights: ClientInsightsData | null = null;
  @Input() loading = false;
  @Input() error = '';

  // These canvases are inside *ngIf blocks; refs may not exist on first render.
  @ViewChild('monthlyChart') monthlyChartRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('balanceChart') balanceChartRef?: ElementRef<HTMLCanvasElement>;

  private charts: Chart[] = [];
  private viewReady = false;
  private rebuildScheduled = false;

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.scheduleRebuild();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['insights'] || changes['isPremium']) {
      this.scheduleRebuild();
    }
  }

  ngOnDestroy(): void {
    this.charts.forEach(c => c.destroy());
  }

  private scheduleRebuild() {
    if (!this.viewReady) return;
    if (this.rebuildScheduled) return;
    this.rebuildScheduled = true;
    // Defer to allow Angular to render the *ngIf canvas elements before reading @ViewChild refs.
    setTimeout(() => {
      this.rebuildScheduled = false;
      this.rebuildCharts();
    });
  }

  private rebuildCharts() {
    if (!this.viewReady) return;
    this.charts.forEach(c => c.destroy());
    this.charts = [];
    if (!this.isPremium || !this.insights) return;

    this.buildMonthlyTransfersChart();
    this.buildBalanceChart();

    // If canvases weren't present yet (due to *ngIf timing), retry once the view updates.
    if (this.charts.length === 0) {
      this.scheduleRebuild();
    }
  }

  private buildMonthlyTransfersChart() {
    const canvas = this.monthlyChartRef?.nativeElement;
    if (!canvas || !this.insights) return;

    const cfg: ChartConfiguration<'bar'> = {
      type: 'bar',
      data: {
        labels: this.insights.monthlyTransfers.labels,
        datasets: [
          {
            label: 'Envoyé',
            data: this.insights.monthlyTransfers.sent,
            backgroundColor: 'rgba(0, 0, 160, 0.75)',
            borderRadius: 10,
          },
          {
            label: 'Reçu',
            data: this.insights.monthlyTransfers.received,
            backgroundColor: 'rgba(34, 197, 94, 0.7)',
            borderRadius: 10,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: true, position: 'bottom' },
          tooltip: { enabled: true },
        },
        scales: {
          x: { grid: { display: false } },
          y: { beginAtZero: true, grid: { color: 'rgba(148,163,184,0.22)' } },
        },
      },
    };

    this.charts.push(new Chart(canvas, cfg));
  }

  private buildBalanceChart() {
    const canvas = this.balanceChartRef?.nativeElement;
    if (!canvas || !this.insights) return;

    const cfg: ChartConfiguration<'line'> = {
      type: 'line',
      data: {
        labels: this.insights.dailyBalance.labels,
        datasets: [
          {
            label: 'Solde',
            data: this.insights.dailyBalance.values,
            borderColor: 'rgba(0, 0, 160, 1)',
            backgroundColor: 'rgba(0, 0, 160, 0.10)',
            fill: true,
            tension: 0.35,
            pointRadius: 0,
            borderWidth: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { enabled: true },
        },
        scales: {
          x: { grid: { display: false }, ticks: { maxTicksLimit: 8 } },
          y: { grid: { color: 'rgba(148,163,184,0.22)' } },
        },
      },
    };

    this.charts.push(new Chart(canvas, cfg));
  }
}
