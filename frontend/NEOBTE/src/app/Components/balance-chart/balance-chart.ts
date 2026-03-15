import { AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { Virement } from '../../Entities/Interfaces/virement';
import { CommonModule } from '@angular/common';


export interface BalancePoint {
  date: Date;
  balance: number;
  transaction?: Virement;
}

export type ChartPeriod = '7D' | '1M' | '3M' | 'ALL';


@Component({
  selector: 'app-balance-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './balance-chart.html',
  styleUrl: './balance-chart.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BalanceChart implements OnChanges, AfterViewInit, OnDestroy {
  @Input() history: Virement[] = [];
  @Input() currentBalance: number = 0;
  @Input() compteId!: number;
 
  // Reference to the wrapper div — always an HTMLElement, no nativeElement confusion
  @ViewChild('chartWrap') chartWrapRef!: ElementRef<HTMLDivElement>;
 
  period: ChartPeriod = '1M';
  periods: ChartPeriod[] = ['7D', '1M', '3M', 'ALL'];
 
  points: BalancePoint[] = [];
  svgPath = '';
  svgAreaPath = '';
  svgDots: { x: number; y: number; point: BalancePoint }[] = [];
 
  activeTooltip: { x: number; y: number; point: BalancePoint } | null = null;
  // Pre-computed SVG coordinates for the tooltip cross-hair & dot
  tooltipSvgX = 0;
  tooltipSvgY = 0;
 
  // Tracks the rendered SVG viewBox width
  svgWidth = 600;
 
  readonly H = 200;
  readonly PAD_T = 20;
  readonly PAD_B = 40;
 
  yLabels: { y: number; value: number }[] = [];
  xLabels: { x: number; label: string }[] = [];
 
  changeAmount = 0;
  changePct = 0;
  isPositive = true;
 
  totalIn = 0;
  totalOut = 0;
  avgTx = 0;
  txCount = 0;
 
  private resizeObserver?: ResizeObserver;
 
  constructor(private cdr: ChangeDetectorRef) {}
 
  ngAfterViewInit() {
    const wrap = this.chartWrapRef?.nativeElement;
    if (!wrap) return;
 
    this.svgWidth = wrap.clientWidth || 600;
    this.buildChart();
    this.cdr.markForCheck();
 
    this.resizeObserver = new ResizeObserver(entries => {
      const w = entries[0]?.contentRect.width ?? 600;
      if (Math.abs(w - this.svgWidth) > 4) {
        this.svgWidth = w;
        this.buildChart();
        this.cdr.markForCheck();
      }
    });
    this.resizeObserver.observe(wrap);
  }
 
  ngOnChanges(changes: SimpleChanges) {
    if (changes['history'] || changes['currentBalance'] || changes['compteId']) {
      this.buildChart();
    }
  }
 
  ngOnDestroy() {
    this.resizeObserver?.disconnect();
  }
 
  setPeriod(p: ChartPeriod) {
    this.period = p;
    this.buildChart();
    this.cdr.markForCheck();
  }
 
  // ── Build chart data ─────────────────────────────────────
  private buildChart() {
    const filtered = this.filterByPeriod(this.history);
    this.points = this.buildBalanceTimeline(filtered);
    this.computeStats(filtered);
    this.renderSvg();
  }
 
  private filterByPeriod(virements: Virement[]): Virement[] {
    const now = new Date();
    let cutoff: Date;
    switch (this.period) {
      case '7D':  cutoff = new Date(now.getTime() - 7  * 86400000); break;
      case '1M':  cutoff = new Date(now.getTime() - 30 * 86400000); break;
      case '3M':  cutoff = new Date(now.getTime() - 90 * 86400000); break;
      default:    cutoff = new Date(0);
    }
    return virements.filter(v => new Date(v.dateDeVirement) >= cutoff);
  }
 
  private buildBalanceTimeline(virements: Virement[]): BalancePoint[] {
    const sorted = [...virements].sort(
      (a, b) => new Date(a.dateDeVirement).getTime() - new Date(b.dateDeVirement).getTime()
    );
 
    // Reconstruct historical balances by walking backwards from current
    let runningBalance = this.currentBalance;
    const snapshots: { date: Date; balance: number; tx: Virement }[] = [];
    const reversed = [...sorted].reverse();
 
    for (const tx of reversed) {
      const isOut = tx.compteSourceId === this.compteId;
      const impact = isOut ? (tx.totalDebite ?? tx.montant + (tx.frais ?? 0)) : tx.montant;
      snapshots.push({ date: new Date(tx.dateDeVirement), balance: runningBalance, tx });
      runningBalance = isOut ? runningBalance + impact : runningBalance - impact;
    }
 
    snapshots.reverse(); // back to chronological
 
    const result: BalancePoint[] = [];
 
    if (snapshots.length > 0) {
      // Balance just before first tx
      result.push({ date: new Date(snapshots[0].date.getTime() - 1), balance: runningBalance });
      for (const s of snapshots) {
        result.push({ date: s.date, balance: s.balance, transaction: s.tx });
      }
    }
 
    // Current point
    result.push({ date: new Date(), balance: this.currentBalance });
 
    if (result.length < 2) {
      const now = new Date();
      return [
        { date: new Date(now.getTime() - 30 * 86400000), balance: this.currentBalance },
        { date: now, balance: this.currentBalance },
      ];
    }
 
    return result;
  }
 
  private computeStats(virements: Virement[]) {
    let totalIn = 0, totalOut = 0;
    for (const v of virements) {
      if (v.compteDestinationId === this.compteId) totalIn  += v.montant;
      if (v.compteSourceId      === this.compteId) totalOut += (v.totalDebite ?? v.montant);
    }
    this.totalIn  = totalIn;
    this.totalOut = totalOut;
    this.txCount  = virements.length;
    this.avgTx    = virements.length > 0 ? (totalIn + totalOut) / virements.length : 0;
 
    if (this.points.length >= 2) {
      const first = this.points[0].balance;
      const last  = this.points[this.points.length - 1].balance;
      this.changeAmount = last - first;
      this.changePct    = first !== 0 ? ((last - first) / Math.abs(first)) * 100 : 0;
      this.isPositive   = this.changeAmount >= 0;
    }
  }
 
  // ── SVG rendering ─────────────────────────────────────────
  private renderSvg() {
    if (this.points.length < 2) return;
 
    const W    = this.svgWidth;
    const H    = this.H;
    const padL = 4, padR = 4, padT = this.PAD_T, padB = this.PAD_B;
 
    const balances = this.points.map(p => p.balance);
    const minB = Math.min(...balances);
    const maxB = Math.max(...balances);
    const range = maxB - minB || 1;
 
    const times = this.points.map(p => p.date.getTime());
    const minD  = Math.min(...times);
    const maxD  = Math.max(...times);
    const dRange = maxD - minD || 1;
 
    const scaleX = (d: number) => padL + ((d - minD) / dRange) * (W - padL - padR);
    const scaleY = (b: number) => padT + (1 - (b - minB) / range) * (H - padT - padB);
 
    const pts = this.points.map(p => ({ x: scaleX(p.date.getTime()), y: scaleY(p.balance) }));
 
    // Smooth line via cubic bezier
    let path = `M ${pts[0].x} ${pts[0].y}`;
    for (let i = 1; i < pts.length; i++) {
      const prev = pts[i - 1];
      const curr = pts[i];
      const cpX  = (prev.x + curr.x) / 2;
      path += ` C ${cpX} ${prev.y} ${cpX} ${curr.y} ${curr.x} ${curr.y}`;
    }
    this.svgPath = path;
 
    const last  = pts[pts.length - 1];
    const first = pts[0];
    this.svgAreaPath = `${path} L ${last.x} ${H - padB} L ${first.x} ${H - padB} Z`;
 
    this.svgDots = this.points
      .map((p, i) => ({ x: pts[i].x, y: pts[i].y, point: p }))
      .filter(d => d.point.transaction !== undefined);
 
    // Y-axis labels (4 ticks)
    this.yLabels = [];
    for (let i = 0; i <= 3; i++) {
      const val = minB + (range * i) / 3;
      this.yLabels.push({ y: scaleY(val), value: val });
    }
 
    // X-axis labels (up to 5)
    this.xLabels = [];
    const count = Math.min(5, pts.length);
    const step  = Math.max(1, Math.floor((pts.length - 1) / (count - 1)));
    for (let i = 0; i < pts.length; i += step) {
      const d     = this.points[i].date;
      const label = this.period === '7D'
        ? d.toLocaleDateString('fr-TN', { weekday: 'short' })
        : d.toLocaleDateString('fr-TN', { day: 'numeric', month: 'short' });
      this.xLabels.push({ x: pts[i].x, label });
    }
  }
 
  // ── Tooltip (MouseEvent only — no SVGElement passed) ──────
  onMouseMove(event: MouseEvent) {
    const wrap = this.chartWrapRef?.nativeElement;
    if (!wrap) return;
 
    // The SVG fills the wrap div, so use wrap's bounding rect
    const rect   = wrap.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const W      = rect.width;
    const padL = 4, padR = 4, padT = this.PAD_T, padB = this.PAD_B;
 
    if (this.points.length < 2) return;
 
    const times  = this.points.map(p => p.date.getTime());
    const minD   = Math.min(...times);
    const maxD   = Math.max(...times);
    const dRange = maxD - minD || 1;
 
    const ratio      = Math.max(0, Math.min(1, (mouseX - padL) / (W - padL - padR)));
    const targetTime = minD + ratio * dRange;
 
    // Snap to nearest data point
    let closest = 0, minDist = Infinity;
    this.points.forEach((p, i) => {
      const d = Math.abs(p.date.getTime() - targetTime);
      if (d < minDist) { minDist = d; closest = i; }
    });
 
    const balances = this.points.map(p => p.balance);
    const minB  = Math.min(...balances);
    const maxB  = Math.max(...balances);
    const range = maxB - minB || 1;
 
    const scaleX = (d: number) => padL + ((d - minD) / dRange) * (this.svgWidth - padL - padR);
    const scaleY = (b: number) => padT + (1 - (b - minB) / range) * (this.H - padT - padB);
 
    const pt = this.points[closest];
 
    // SVG-space coordinates used directly in template
    this.tooltipSvgX = scaleX(pt.date.getTime());
    this.tooltipSvgY = scaleY(pt.balance);
 
    // Percentage position for the CSS-positioned tooltip bubble
    const xPct = (this.tooltipSvgX / this.svgWidth) * 100;
    const yPct = (this.tooltipSvgY / this.H) * 100;
 
    this.activeTooltip = { x: xPct, y: yPct, point: pt };
    this.cdr.markForCheck();
  }
 
  onMouseLeave() {
    this.activeTooltip = null;
    this.cdr.markForCheck();
  }
 
  // ── Helpers ───────────────────────────────────────────────
  formatBalance(v: number): string {
    return v.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
  }
 
  formatDate(d: Date): string {
    return new Date(d).toLocaleDateString('fr-TN', {
      day: 'numeric', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }
 
  formatPeriodLabel(p: ChartPeriod): string {
    switch (p) {
      case '7D':  return '7 jours';
      case '1M':  return '1 mois';
      case '3M':  return '3 mois';
      case 'ALL': return 'Tout';
    }
  }
}

