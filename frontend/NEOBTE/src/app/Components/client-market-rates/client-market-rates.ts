import { CommonModule } from '@angular/common';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subscription, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { MarketRatesResponse } from '../../Entities/Interfaces/market-rates';
import { ClientMarketService } from '../../Services/client-market.service';

@Component({
  selector: 'app-client-market-rates',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './client-market-rates.html',
  styleUrl: './client-market-rates.css',
})
export class ClientMarketRates implements OnInit, OnDestroy {
  private _isPremium = false;
  @Input()
  set isPremium(v: boolean) {
    const next = !!v;
    const prev = this._isPremium;
    this._isPremium = next;
    if (!prev && next) this.start();
    if (prev && !next) this.stop();
  }
  get isPremium(): boolean { return this._isPremium; }

  loading = false;
  error = '';
  data: MarketRatesResponse | null = null;

  private sub: Subscription | null = null;

  constructor(private market: ClientMarketService) {}

  ngOnInit(): void {
    if (this.isPremium) this.start();
  }

  ngOnDestroy(): void {
    this.stop();
  }

  refresh() {
    if (!this.isPremium) return;
    this.loading = true;
    this.error = '';
    this.market.getRates().subscribe({
      next: (d) => {
        this.data = d;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = this.extractErrorMessage(err) || "Impossible de charger les taux en ce moment.";
      }
    });
  }

  get updatedLabel(): string {
    if (!this.data?.generatedAt) return '';
    const d = new Date(this.data.generatedAt);
    if (Number.isNaN(d.getTime())) return '';
    return d.toLocaleString('fr-TN', { dateStyle: 'medium', timeStyle: 'short' });
  }

  private extractErrorMessage(err: any): string {
    const e = err?.error;
    if (!e) return '';
    if (typeof e === 'string') return e;
    return e.message || e.detail || '';
  }

  private start() {
    if (this.sub) return;
    this.loading = true;
    this.error = '';
    // Quasi temps réel: refresh toutes les 60s
    this.sub = timer(0, 60000).pipe(
      switchMap(() => this.market.getRates())
    ).subscribe({
      next: (d) => {
        this.data = d;
        this.error = '';
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = this.extractErrorMessage(err) || "Impossible de charger les taux en ce moment.";
      }
    });
  }

  private stop() {
    this.sub?.unsubscribe();
    this.sub = null;
    this.loading = false;
    this.error = '';
    this.data = null;
  }
}
