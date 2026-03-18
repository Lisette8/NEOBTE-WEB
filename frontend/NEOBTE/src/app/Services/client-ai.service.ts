import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ClientChatMessage,
  ClientChatResponse,
  ClientInsightsData,
  PremiumStatus,
} from '../Entities/Interfaces/client-premium';
import { VirementService } from './virement.service';

@Injectable({ providedIn: 'root' })
export class ClientAiService {

  private readonly base = 'http://localhost:8080/api/v1/client/ai';

  constructor(
    private http: HttpClient,
    private virementService: VirementService
  ) {}

  getPremiumStatus(): Observable<PremiumStatus> {
    return this.http.get<PremiumStatus>(`${this.base}/premium/status`);
  }

  chat(message: string, history: ClientChatMessage[]): Observable<ClientChatResponse> {
    return this.http.post<ClientChatResponse>(`${this.base}/chat`, { message, history });
  }

  /**
   * Client insights are computed from transfer history client-side (no dedicated backend endpoint).
   * This keeps the feature working even if analytics endpoints are admin-only.
   */
  getClientInsights(accountIds: number[], currentTotalBalance: number, days: number = 30): Observable<ClientInsightsData> {
    const accountSet = new Set<number>(accountIds ?? []);

    return this.virementService.getHistory().pipe(
      map(history => {
        const now = new Date();
        const startDate = new Date(now.getTime() - (days - 1) * 86400000);

        const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
        let sentThisMonth = 0;
        let receivedThisMonth = 0;
        let txCountThisMonth = 0;

        // ── Monthly aggregates (last 6 months) ───────────────────────────────
        const monthBuckets = new Map<string, { sent: number; received: number }>();
        for (let i = 5; i >= 0; i--) {
          const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
          monthBuckets.set(this.monthKey(d), { sent: 0, received: 0 });
        }

        // ── Daily net movement for balance reconstruction ───────────────────
        const dayKeys: string[] = [];
        const netByDay = new Map<string, number>(); // +in -out
        for (let i = 0; i < days; i++) {
          const d = new Date(startDate.getTime() + i * 86400000);
          const k = this.dayKey(d);
          dayKeys.push(k);
          netByDay.set(k, 0);
        }

        for (const tx of (history ?? [])) {
          const dt = new Date(tx.dateDeVirement);
          if (isNaN(dt.getTime())) continue;

          const isOutgoing = accountSet.has(tx.compteSourceId);
          const isIncoming = accountSet.has(tx.compteDestinationId);

          // Ignore transfers that are not related to this user's accounts
          if (!isOutgoing && !isIncoming) continue;

          const outImpact = (tx.totalDebite ?? (tx.montant + (tx.frais ?? 0))) || 0;
          const inImpact = (tx.montant ?? 0) || 0;

          // Monthly chart buckets
          const mKey = this.monthKey(dt);
          const bucket = monthBuckets.get(mKey);
          if (bucket) {
            if (isOutgoing) bucket.sent += outImpact;
            if (isIncoming) bucket.received += inImpact;
          }

          // This month summary
          if (dt >= monthStart) {
            txCountThisMonth++;
            if (isOutgoing) sentThisMonth += outImpact;
            if (isIncoming) receivedThisMonth += inImpact;
          }

          // Daily net for balance chart
          const dKey = this.dayKey(dt);
          if (netByDay.has(dKey)) {
            const net = (netByDay.get(dKey) ?? 0) + (isIncoming ? inImpact : 0) - (isOutgoing ? outImpact : 0);
            netByDay.set(dKey, net);
          }
        }

        const monthlyLabels: string[] = [];
        const monthlySent: number[] = [];
        const monthlyReceived: number[] = [];

        for (const [k, v] of monthBuckets.entries()) {
          monthlyLabels.push(this.monthLabelFromKey(k));
          monthlySent.push(this.round2(v.sent));
          monthlyReceived.push(this.round2(v.received));
        }

        // Balance at end of day (approx): walk backwards from current balance.
        const balances: number[] = new Array(days).fill(0);
        let running = currentTotalBalance ?? 0;
        for (let i = days - 1; i >= 0; i--) {
          const k = dayKeys[i];
          balances[i] = this.round2(running);
          running = running - (netByDay.get(k) ?? 0);
        }

        const dailyLabels = dayKeys.map(k => this.shortDayLabelFromKey(k));

        return {
          monthlyTransfers: {
            labels: monthlyLabels,
            sent: monthlySent,
            received: monthlyReceived,
          },
          dailyBalance: {
            labels: dailyLabels,
            values: balances,
          },
          summary: {
            sentThisMonth: this.round2(sentThisMonth),
            receivedThisMonth: this.round2(receivedThisMonth),
            txCountThisMonth,
          },
          generatedAt: new Date().toISOString(),
        };
      })
    );
  }

  private monthKey(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    return `${y}-${m}`;
  }

  private monthLabelFromKey(key: string): string {
    const [y, m] = key.split('-').map(Number);
    const d = new Date(y, (m ?? 1) - 1, 1);
    return d.toLocaleString(undefined, { month: 'short', year: 'numeric' });
  }

  private dayKey(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  private shortDayLabelFromKey(key: string): string {
    const parts = key.split('-').map(Number);
    const d = new Date(parts[0], (parts[1] ?? 1) - 1, parts[2] ?? 1);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    return `${dd}/${mm}`;
  }

  private round2(v: number): number {
    return Math.round((v ?? 0) * 100) / 100;
  }
}
