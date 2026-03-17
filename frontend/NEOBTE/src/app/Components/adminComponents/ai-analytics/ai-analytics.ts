import {
  AfterViewInit, Component, ElementRef, OnDestroy, OnInit,
  ViewChild, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Chart, ChartConfiguration,
  LineController, BarController, DoughnutController,
  CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement,
  Tooltip, Legend, Filler
} from 'chart.js';
import { AiAnalyticsService } from '../../../Services/ai-analytics.service';
import { AnalyticsData, AiInsights, ChatMessage } from '../../../Entities/Interfaces/ai-analytics';

Chart.register(
  LineController, BarController, DoughnutController,
  CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement,
  Tooltip, Legend, Filler
);

export type AiTab = 'analytics' | 'fraud' | 'chat';

@Component({
  selector: 'app-ai-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-analytics.html',
  styleUrl: './ai-analytics.css',
})
export class AiAnalytics implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('transferChart')   transferChartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('userGrowthChart') userGrowthRef!:    ElementRef<HTMLCanvasElement>;
  @ViewChild('fraudTypeChart')  fraudTypeRef!:     ElementRef<HTMLCanvasElement>;
  @ViewChild('fraudSevChart')   fraudSevRef!:      ElementRef<HTMLCanvasElement>;
  @ViewChild('fraudTrendChart') fraudTrendRef!:    ElementRef<HTMLCanvasElement>;
  @ViewChild('chatScroll')      chatScrollRef!:    ElementRef<HTMLDivElement>;

  // ── Tab state ────────────────────────────────────────────────────────────
  activeTab: AiTab = 'analytics';

  // ── Analytics ────────────────────────────────────────────────────────────
  analytics: AnalyticsData | null = null;
  loadingAnalytics = true;
  analyticsError   = '';
  private charts: Chart[] = [];

  // ── AI Report (Fraud Intel tab) ──────────────────────────────────────────
  insights: AiInsights | null = null;
  loadingInsights = false;
  insightsError   = '';

  // ── Chatbot ──────────────────────────────────────────────────────────────
  messages: ChatMessage[] = [];
  inputMessage = '';
  chatLoading  = false;
  chatError    = '';

  readonly suggestedQuestions = [
    'What is the current fraud risk level?',
    'Summarize transfer activity this month',
    'Which users should I monitor closely?',
    'What is the forecasted transfer volume?',
    'How is client growth trending?',
  ];

  constructor(
    private aiService: AiAnalyticsService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit()       { this.loadAnalytics(); }
  ngAfterViewInit(){ /* charts built after data */ }
  ngOnDestroy()    { this.charts.forEach(c => c.destroy()); }

  // ── Tab switching ────────────────────────────────────────────────────────
  switchTab(tab: AiTab) {
    this.activeTab = tab;
    if (tab === 'analytics' || tab === 'fraud') {
      // Rebuild charts after DOM renders
      setTimeout(() => this.buildAllCharts(), 60);
    }
  }

  // ── Analytics loading ────────────────────────────────────────────────────
  loadAnalytics() {
    this.loadingAnalytics = true;
    this.analyticsError = '';
    this.aiService.getAnalytics().subscribe({
      next: (data) => {
        this.analytics = data;
        this.loadingAnalytics = false;
        setTimeout(() => this.buildAllCharts(), 60);
      },
      error: () => {
        this.analyticsError = 'Failed to load analytics data.';
        this.loadingAnalytics = false;
      }
    });
  }

  // ── AI Report ────────────────────────────────────────────────────────────
  fetchInsights() {
    this.loadingInsights = true;
    this.insightsError = '';
    this.insights = null;
    this.aiService.getInsights().subscribe({
      next:  (data) => { this.insights = data;  this.loadingInsights = false; },
      error: ()     => { this.insightsError = 'AI unavailable. Check GROQ_API_KEY.'; this.loadingInsights = false; }
    });
  }

  // ── Chatbot ──────────────────────────────────────────────────────────────
  useSuggestion(q: string) {
    this.inputMessage = q;
    this.sendMessage();
  }

  sendMessage() {
    const text = this.inputMessage.trim();
    if (!text || this.chatLoading) return;

    this.messages.push({ role: 'user', content: text });
    this.inputMessage = '';
    this.chatLoading  = true;
    this.chatError    = '';
    this.scrollChat();

    // Send only the last 10 turns to stay within token limits
    const history = this.messages.slice(0, -1).slice(-10);

    this.aiService.chat(text, history).subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', content: res.reply });
        this.chatLoading = false;
        this.scrollChat();
      },
      error: () => {
        this.chatError    = 'Could not reach the AI assistant. Check GROQ_API_KEY.';
        this.chatLoading  = false;
        // Remove the user message that failed
        this.messages.pop();
      }
    });
  }

  clearChat() {
    this.messages    = [];
    this.chatError   = '';
    this.inputMessage = '';
  }

  onInputKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); this.sendMessage(); }
  }

  private scrollChat() {
    setTimeout(() => {
      const el = this.chatScrollRef?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    }, 30);
  }

  // ── Chart builders ───────────────────────────────────────────────────────
  private buildAllCharts() {
    if (!this.analytics) return;
    this.charts.forEach(c => c.destroy());
    this.charts = [];

    this.buildTransferChart();
    this.buildUserGrowthChart();
    this.buildFraudTypeChart();
    this.buildFraudSevChart();
    this.buildFraudTrendChart();
  }

  private buildTransferChart() {
    const canvas = this.transferChartRef?.nativeElement;
    if (!canvas || !this.analytics) return;

    const actual  = this.analytics.dailyTransfers;
    const forecast = this.analytics.forecast;

    const labels = [
      ...actual.map(d => d.date.substring(5)),
      ...forecast.map(d => d.date.substring(5)),
    ];

    const cfg: ChartConfiguration<'line'> = {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Transfer Volume (TND)',
            data: [...actual.map(d => d.totalAmount), ...new Array(forecast.length).fill(null)],
            borderColor: '#6366f1',
            backgroundColor: 'rgba(99,102,241,0.12)',
            fill: true,
            tension: 0.4,
            pointRadius: 3,
            pointBackgroundColor: '#6366f1',
          },
          {
            label: '7-day Forecast',
            data: [...new Array(actual.length).fill(null), ...forecast.map(d => d.totalAmount)],
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245,158,11,0.08)',
            borderDash: [6, 3],
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#f59e0b',
          },
        ],
      },
      options: this.lineOptions('Daily Transfer Volume & 7-Day Forecast (TND)'),
    };
    this.charts.push(new Chart(canvas, cfg));
  }

  private buildUserGrowthChart() {
    const canvas = this.userGrowthRef?.nativeElement;
    if (!canvas || !this.analytics) return;

    const data = this.analytics.monthlyUsers;
    const cfg: ChartConfiguration<'bar'> = {
      type: 'bar',
      data: {
        labels: data.map(d => d.month),
        datasets: [{
          label: 'New Clients',
          data: data.map(d => d.count),
          backgroundColor: 'rgba(16,185,129,0.75)',
          borderColor: '#10b981',
          borderWidth: 1,
          borderRadius: 6,
        }],
      },
      options: this.barOptions('Monthly Client Registrations'),
    };
    this.charts.push(new Chart(canvas, cfg));
  }

  private buildFraudTypeChart() {
    const canvas = this.fraudTypeRef?.nativeElement;
    if (!canvas || !this.analytics) return;

    const data = this.analytics.fraudByType;
    const cfg: ChartConfiguration<'doughnut'> = {
      type: 'doughnut',
      data: {
        labels: data.map(d => this.typeLabel(d.label)),
        datasets: [{
          data: data.map(d => d.count),
          backgroundColor: ['#6366f1','#f59e0b','#ef4444','#10b981','#3b82f6'],
          hoverOffset: 8,
        }],
      },
      options: this.doughnutOptions('Fraud Alerts by Type'),
    };
    this.charts.push(new Chart(canvas, cfg));
  }

  private buildFraudSevChart() {
    const canvas = this.fraudSevRef?.nativeElement;
    if (!canvas || !this.analytics) return;

    const data = this.analytics.fraudBySeverity;
    const colorMap: Record<string, string> = {
      HIGH:   '#ef4444',
      MEDIUM: '#f59e0b',
      LOW:    '#10b981',
    };
    const cfg: ChartConfiguration<'doughnut'> = {
      type: 'doughnut',
      data: {
        labels: data.map(d => d.label),
        datasets: [{
          data: data.map(d => d.count),
          backgroundColor: data.map(d => colorMap[d.label] ?? '#6366f1'),
          hoverOffset: 8,
        }],
      },
      options: this.doughnutOptions('Fraud Alerts by Severity'),
    };
    this.charts.push(new Chart(canvas, cfg));
  }

  private buildFraudTrendChart() {
    const canvas = this.fraudTrendRef?.nativeElement;
    if (!canvas || !this.analytics) return;

    const data = this.analytics.fraudTrend;
    const cfg: ChartConfiguration<'line'> = {
      type: 'line',
      data: {
        labels: data.map(d => d.date.substring(5)),
        datasets: [{
          label: 'Fraud Alerts',
          data: data.map(d => d.count),
          borderColor: '#ef4444',
          backgroundColor: 'rgba(239,68,68,0.10)',
          fill: true,
          tension: 0.4,
          pointRadius: 3,
          pointBackgroundColor: '#ef4444',
        }],
      },
      options: this.lineOptions('Daily Fraud Alert Trend (30 Days)'),
    };
    this.charts.push(new Chart(canvas, cfg));
  }

  // ── Shared chart option factories ────────────────────────────────────────
  private lineOptions(title: string): ChartConfiguration<'line'>['options'] {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top', labels: { color: '#334155', font: { size: 11 } } },
        tooltip: { mode: 'index', intersect: false },
      },
      scales: {
        x: { ticks: { color: '#64748b', maxRotation: 45 }, grid: { color: 'rgba(0,0,0,0.06)' } },
        y: { ticks: { color: '#64748b' }, grid: { color: 'rgba(0,0,0,0.06)' }, beginAtZero: true },
      },
    };
  }

  private barOptions(title: string): ChartConfiguration<'bar'>['options'] {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top', labels: { color: '#334155', font: { size: 11 } } },
      },
      scales: {
        x: { ticks: { color: '#64748b' }, grid: { color: 'rgba(0,0,0,0.06)' } },
        y: { ticks: { color: '#64748b' }, grid: { color: 'rgba(0,0,0,0.06)' }, beginAtZero: true },
      },
    };
  }

  private doughnutOptions(title: string): ChartConfiguration<'doughnut'>['options'] {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'right', labels: { color: '#334155', font: { size: 11 }, padding: 12 } },
        tooltip: { callbacks: { label: ctx => ` ${ctx.label}: ${ctx.parsed}` } },
      },
      cutout: '65%',
    };
  }

  // ── Helpers ──────────────────────────────────────────────────────────────
  riskClass(level: string): string {
    switch (level) {
      case 'HIGH':   return 'risk-high';
      case 'MEDIUM': return 'risk-medium';
      default:       return 'risk-low';
    }
  }

  typeLabel(t: string): string {
    const map: Record<string, string> = {
      LARGE_SINGLE_TRANSFER: 'Large Transfer',
      RAPID_SUCCESSION:      'Rapid Succession',
      SUSPICIOUS_HOUR:       'Suspicious Hour',
      DAILY_COUNT_EXCEEDED:  'Daily Count',
      DAILY_AMOUNT_EXCEEDED: 'Daily Amount',
    };
    return map[t] ?? t;
  }

  fmtAmount(v: number): string {
    return v.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
  }
}
