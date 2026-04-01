import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import {
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  ChartDataset,
  Filler,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
} from 'chart.js';

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
  Filler,
);

/** Emotional colour palette — each mood maps to a vivid identity */
const MOODS = {
  growth: {
    line: '#7c3aed',
    gradTop: 'rgba(124,58,237,0.35)',
    gradBot: 'rgba(124,58,237,0)',
    barA: 'rgba(124,58,237,0.82)',
    barB: 'rgba(167,139,250,0.72)',
  },
  balance: {
    line: '#0d2b77',
    gradTop: 'rgba(13,43,119,0.32)',
    gradBot: 'rgba(13,43,119,0)',
    barA: 'rgba(13,43,119,0.80)',
    barB: 'rgba(59,130,246,0.72)',
  },
  transfers: {
    line: '#ba9553',
    gradTop: 'rgba(186,149,83,0.30)',
    gradBot: 'rgba(186,149,83,0)',
    barA: 'rgba(186,149,83,0.85)',   // sent  — gold
    barB: 'rgba(16,185,129,0.78)',   // received — emerald
  },
} as const;

export type NeoChartMood = keyof typeof MOODS;
export type NeoChartType = 'area' | 'bar';

export interface NeoChartDataset {
  label: string;
  data: number[];
}

@Component({
  selector: 'app-neo-chart',
  standalone: true,
  imports: [],
  templateUrl: './neo-chart.html',
  styleUrl: './neo-chart.css',
})
export class NeoChart implements AfterViewInit, OnChanges, OnDestroy {
  /** Chart variant */
  @Input() type: NeoChartType = 'area';

  /**
   * Emotional colour palette to use.
   * - 'growth'    → violet/purple (profile / balance growth)
   * - 'balance'   → navy         (balance trend)
   * - 'transfers' → gold + emerald (sent / received)
   */
  @Input() mood: NeoChartMood = 'growth';

  /** X-axis labels */
  @Input() labels: string[] = [];

  /**
   * For 'area' charts: single value series.
   * For 'bar' charts: pass `datasets` instead.
   */
  @Input() values: number[] = [];

  /**
   * For 'bar' charts with multiple series (e.g. sent + received).
   * Ignored for 'area' charts.
   */
  @Input() datasets: NeoChartDataset[] = [];

  /**
   * When true, axes and legend are hidden — ideal for compact dashboard cards.
   * When false, full axes + legend are shown.
   */
  @Input() compact = false;

  @ViewChild('chartCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  private chart: Chart | null = null;
  private built = false;

  ngAfterViewInit(): void {
    this.built = true;
    this.rebuild();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.built) {
      // Defer so Angular finishes rendering before we read the canvas
      setTimeout(() => this.rebuild());
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
    this.chart = null;
  }

  private rebuild(): void {
    const canvas = this.canvasRef?.nativeElement;
    if (!canvas) return;

    this.chart?.destroy();
    this.chart = null;

    const palette = MOODS[this.mood];

    if (this.type === 'area') {
      this.buildArea(canvas, palette);
    } else {
      this.buildBar(canvas, palette);
    }
  }

  private buildArea(canvas: HTMLCanvasElement, p: (typeof MOODS)[NeoChartMood]): void {
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Gradient fill
    const grad = ctx.createLinearGradient(0, 0, 0, canvas.clientHeight || 200);
    grad.addColorStop(0, p.gradTop);
    grad.addColorStop(1, p.gradBot);

    const dataset = {
      label: 'Solde',
      data: this.values,
      borderColor: p.line,
      backgroundColor: grad,
      fill: true,
      tension: 0.45,
      borderWidth: 2.5,
      pointRadius: this.compact ? 0 : 4,
      pointBackgroundColor: '#fff',
      pointBorderColor: p.line,
      pointBorderWidth: 2,
      pointHoverRadius: this.compact ? 0 : 6,
    };

    this.chart = new Chart(canvas, {
      type: 'line',
      data: { labels: this.labels, datasets: [dataset] },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: { duration: 600, easing: 'easeInOutQuart' },
        plugins: {
          legend: { display: false },
          tooltip: {
            enabled: true,
            backgroundColor: 'rgba(12,20,44,0.88)',
            titleColor: 'rgba(255,255,255,0.65)',
            bodyColor: '#fff',
            padding: 12,
            cornerRadius: 10,
            displayColors: false,
            callbacks: {
              label: (ctx) => `  ${(ctx.parsed.y as number).toLocaleString('fr-TN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} TND`,
            },
          },
        },
        scales: {
          x: {
            display: !this.compact,
            grid: { display: false },
            ticks: {
              color: '#8898b3',
              font: { size: 11, family: 'Inter, sans-serif' },
              maxTicksLimit: 7,
              maxRotation: 0,
            },
            border: { display: false },
          },
          y: {
            display: !this.compact,
            grid: { color: 'rgba(13,43,119,0.07)' },
            ticks: {
              color: '#8898b3',
              font: { size: 11, family: 'Inter, sans-serif' },
              maxTicksLimit: 4,
              callback: (v) => {
                const n = Number(v);
                return n >= 1000 ? `${(n / 1000).toFixed(0)}k` : `${n}`;
              },
            },
            border: { display: false },
          },
        },
      },
    });
  }

  private buildBar(canvas: HTMLCanvasElement, p: (typeof MOODS)[NeoChartMood]): void {
    const barColors = [p.barA, p.barB];
    const src = this.datasets.length > 0 ? this.datasets : [{ label: 'Valeur', data: this.values }];

    const builtDatasets: ChartDataset<'bar'>[] = src.map((ds, i) => ({
      label: ds.label,
      data: ds.data,
      backgroundColor: barColors[i % barColors.length],
      borderRadius: 8,
      borderSkipped: false,
    }));

    this.chart = new Chart(canvas, {
      type: 'bar',
      data: { labels: this.labels, datasets: builtDatasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: { duration: 600, easing: 'easeInOutQuart' },
        plugins: {
          legend: {
            display: !this.compact,
            position: 'bottom',
            labels: {
              color: '#5f6d89',
              font: { size: 12, family: 'Inter, sans-serif' },
              boxWidth: 12,
              boxHeight: 12,
              borderRadius: 4,
              useBorderRadius: true,
              padding: 16,
            },
          },
          tooltip: {
            backgroundColor: 'rgba(12,20,44,0.88)',
            titleColor: 'rgba(255,255,255,0.65)',
            bodyColor: '#fff',
            padding: 12,
            cornerRadius: 10,
          },
        },
        scales: {
          x: {
            display: !this.compact,
            grid: { display: false },
            ticks: {
              color: '#8898b3',
              font: { size: 11, family: 'Inter, sans-serif' },
              maxRotation: 0,
            },
            border: { display: false },
          },
          y: {
            display: !this.compact,
            beginAtZero: true,
            grid: { color: 'rgba(13,43,119,0.07)' },
            ticks: {
              color: '#8898b3',
              font: { size: 11, family: 'Inter, sans-serif' },
              maxTicksLimit: 4,
              callback: (v) => {
                const n = Number(v);
                return n >= 1000 ? `${(n / 1000).toFixed(0)}k` : `${n}`;
              },
            },
            border: { display: false },
          },
        },
      },
    });
  }
}
