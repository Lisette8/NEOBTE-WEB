import { Injectable } from "@angular/core";
import { Observable, of, forkJoin, catchError, map } from "rxjs";
import { ACCOUNT_TYPE_META } from "../Entities/Interfaces/compte";
import { LOAN_TYPE_LABELS, LOAN_STATUT_LABELS } from "../Entities/Interfaces/loan";
import { CompteService } from "./compte-service";
import { InvestmentService } from "./investment.service";
import { LoanService } from "./loan.service";
import { VirementService } from "./virement.service";

export type SearchResultKind = 'compte' | 'virement' | 'placement' | 'pret';

export interface SearchResult {
    kind: SearchResultKind;
    icon: string;
    title: string;
    subtitle: string;
    badge?: string;
    badgeClass?: string;
    route: string[];
    queryParams?: Record<string, string>;
}

@Injectable({ providedIn: 'root' })
export class ClientSearchService {

    constructor(
        private compteService: CompteService,
        private virementService: VirementService,
        private investmentService: InvestmentService,
        private loanService: LoanService,
    ) { }

    search(query: string): Observable<SearchResult[]> {
        const q = query.trim().toLowerCase();
        if (q.length < 2) return of([]);

        return forkJoin({
            comptes: this.compteService.getMyAccounts().pipe(catchError(() => of([]))),
            virements: this.virementService.getHistory().pipe(catchError(() => of([]))),
            placements: this.investmentService.getMyInvestments().pipe(catchError(() => of([]))),
            prets: this.loanService.getMyLoans().pipe(catchError(() => of([]))),
        }).pipe(
            map(({ comptes, virements, placements, prets }) => {
                const results: SearchResult[] = [];

                // ── Comptes ─────────────────────────────────────────────────────────
                for (const c of comptes) {
                    const meta = ACCOUNT_TYPE_META[c.typeCompte];
                    const label = meta?.label ?? c.typeCompte;
                    const idStr = String(c.idCompte);
                    if (
                        label.toLowerCase().includes(q) ||
                        c.typeCompte.toLowerCase().includes(q) ||
                        idStr.includes(q) ||
                        c.statutCompte.toLowerCase().includes(q)
                    ) {
                        results.push({
                            kind: 'compte',
                            icon: 'fa-solid fa-credit-card',
                            title: label,
                            subtitle: `Compte #${c.idCompte} · ${c.solde.toLocaleString('fr-TN', { minimumFractionDigits: 3 })} TND`,
                            badge: c.statutCompte === 'ACTIVE' ? 'Actif' : c.statutCompte,
                            badgeClass: c.statutCompte === 'ACTIVE' ? 'badge--green' : 'badge--grey',
                            route: ['/account', String(c.idCompte)],
                        });
                    }
                }

                // ── Virements ────────────────────────────────────────────────────────
                for (const v of virements) {
                    const montantStr = v.montant.toLocaleString('fr-TN', { minimumFractionDigits: 3 });
                    const date = new Date(v.dateDeVirement).toLocaleDateString('fr-TN', { day: '2-digit', month: 'short', year: 'numeric' });
                    if (
                        String(v.idVirement).includes(q) ||
                        (v.recipientName ?? '').toLowerCase().includes(q) ||
                        (v.senderName ?? '').toLowerCase().includes(q) ||
                        montantStr.includes(q) ||
                        'virement'.includes(q)
                    ) {
                        results.push({
                            kind: 'virement',
                            icon: 'fa-solid fa-right-left',
                            title: `Virement #${v.idVirement}`,
                            subtitle: `${v.montant.toLocaleString('fr-TN', { minimumFractionDigits: 3 })} TND · ${date}`,
                            badge: v.recipientName ? `→ ${v.recipientName}` : undefined,
                            badgeClass: 'badge--blue',
                            route: ['/virement-view'],
                        });
                    }
                }

                // ── Placements ───────────────────────────────────────────────────────
                for (const p of placements) {
                    if (
                        (p.planNom ?? '').toLowerCase().includes(q) ||
                        String(p.id).includes(q) ||
                        'placement'.includes(q) ||
                        p.statut.toLowerCase().includes(q)
                    ) {
                        const statutLabel = p.statut === 'ACTIVE' ? 'Actif' : p.statut === 'COMPLETED' ? 'Complété' : 'Annulé';
                        results.push({
                            kind: 'placement',
                            icon: 'fa-solid fa-chart-line',
                            title: p.planNom,
                            subtitle: `${p.montant.toLocaleString('fr-TN', { minimumFractionDigits: 3 })} TND · ${p.tauxAnnuel}% / an`,
                            badge: statutLabel,
                            badgeClass: p.statut === 'ACTIVE' ? 'badge--green' : 'badge--grey',
                            route: ['/investment-view'],
                        });
                    }
                }

                // ── Prêts ────────────────────────────────────────────────────────────
                for (const l of prets) {
                    const typeLabel = LOAN_TYPE_LABELS[l.type] ?? l.type;
                    const statutLabel = LOAN_STATUT_LABELS[l.statut]?.label ?? l.statut;
                    if (
                        typeLabel.toLowerCase().includes(q) ||
                        (l.productNom ?? '').toLowerCase().includes(q) ||
                        String(l.id).includes(q) ||
                        'prêt'.includes(q) ||
                        'pret'.includes(q) ||
                        l.statut.toLowerCase().includes(q)
                    ) {
                        results.push({
                            kind: 'pret',
                            icon: 'fa-solid fa-hand-holding-dollar',
                            title: l.productNom ?? typeLabel,
                            subtitle: `${l.montant.toLocaleString('fr-TN', { minimumFractionDigits: 3 })} TND · Reste : ${l.resteADu.toLocaleString('fr-TN', { minimumFractionDigits: 3 })} TND`,
                            badge: statutLabel,
                            badgeClass: l.statut === 'ACTIVE' ? 'badge--green' : l.statut === 'PENDING_APPROVAL' ? 'badge--amber' : 'badge--grey',
                            route: ['/loan-view'],
                        });
                    }
                }

                // Cap at 8 results
                return results.slice(0, 8);
            })
        );
    }
}
