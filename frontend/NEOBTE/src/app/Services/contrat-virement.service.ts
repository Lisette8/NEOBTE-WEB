import { Injectable } from "@angular/core";
import { ClientProfile } from "../Entities/Interfaces/client-profile";
import { Virement } from "../Entities/Interfaces/virement";
import { Investment } from "../Entities/Interfaces/investment";
import { Loan, LOAN_TYPE_LABELS } from "../Entities/Interfaces/loan";

export interface ContratConfig {
    bankName: string;
    bankLegalName: string;
    bankAddress: string;
    bankWebsite: string;
    brandColor: string;
    accentColor: string;
    legalNote: string;
}

export const DEFAULT_CONTRAT_CONFIG: ContratConfig = {
    bankName: 'NEO BTE',
    bankLegalName: 'Banque de Tunisie et des Émirats',
    bankAddress: 'Boulevard Beji Caid Essebsi – Centre Urbain Nord – 1082 Tunis',
    bankWebsite: 'www.bte.com.tn',
    brandColor: '#0d2b77',
    accentColor: '#ba9553',
    legalNote:
        'Ce document constitue un justificatif officiel émis par NEO BTE. ' +
        'Il est généré automatiquement et ne nécessite pas de signature. ' +
        'Conservez-le comme preuve de transaction. ' +
        'Pour toute contestation, contactez votre agence BTE dans les 30 jours.',
};

@Injectable({ providedIn: 'root' })
export class ContratVirementService {

    // ── Public API ────────────────────────────────────────────────────────────

    printVirement(v: Virement, profile: ClientProfile | null, mode: 'externe' | 'interne', cfg = DEFAULT_CONTRAT_CONFIG) {
        this.open(this.buildVirementHtml(v, profile, mode, cfg));
    }

    printLoan(loan: Loan, profile: ClientProfile | null, cfg = DEFAULT_CONTRAT_CONFIG) {
        this.open(this.buildLoanHtml(loan, profile, cfg));
    }

    printPlacement(inv: Investment, profile: ClientProfile | null, cfg = DEFAULT_CONTRAT_CONFIG) {
        this.open(this.buildPlacementHtml(inv, profile, cfg));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private open(html: string) {
        const win = window.open('', '_blank', 'width=860,height=1100');
        if (!win) return;
        win.document.open();
        win.document.write(html);
        win.document.close();
        win.onload = () => setTimeout(() => { win.focus(); win.print(); }, 400);
    }

    private fmt(n: number): string {
        return (n ?? 0).toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
    }

    private fmtDate(iso: string | undefined | null, opts?: Intl.DateTimeFormatOptions): string {
        if (!iso) return '—';
        try {
            return new Date(iso).toLocaleDateString('fr-TN', opts ?? { day: '2-digit', month: 'long', year: 'numeric' });
        } catch { return iso; }
    }

    private fullName(profile: ClientProfile | null): string {
        if (!profile) return '—';
        return `${profile.prenom ?? ''} ${profile.nom ?? ''}`.trim() || '—';
    }

    // ── Base HTML shell ───────────────────────────────────────────────────────

    private shell(cfg: ContratConfig, title: string, ref: string, dateStr: string, timeStr: string, body: string): string {
        return `<!DOCTYPE html>
  <html lang="fr"><head>
  <meta charset="UTF-8">
  <title>${title} — ${ref}</title>
  <style>
  *,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
  body{font-family:'Segoe UI',-apple-system,BlinkMacSystemFont,Arial,sans-serif;background:#f4f6fb;color:#12203d;font-size:13px;line-height:1.5;padding:32px 24px}
  .page{max-width:720px;margin:0 auto;background:#fff;border-radius:16px;box-shadow:0 4px 32px rgba(13,43,119,.12);overflow:hidden}
  .hdr{background:${cfg.brandColor};padding:26px 36px 22px;display:flex;align-items:flex-start;justify-content:space-between;gap:16px}
  .hdr-mark{width:40px;height:40px;border-radius:11px;background:rgba(255,255,255,.15);border:1.5px solid rgba(255,255,255,.25);display:flex;align-items:center;justify-content:center;font-size:17px;font-weight:900;color:#fff}
  .hdr-name{font-size:1.25rem;font-weight:800;color:#fff;letter-spacing:-.02em}
  .hdr-name span{color:${cfg.accentColor}}
  .hdr-legal{font-size:.7rem;color:rgba(255,255,255,.6);margin-top:2px}
  .hdr-right{text-align:right}
  .hdr-type{font-size:.68rem;font-weight:700;letter-spacing:.1em;text-transform:uppercase;color:${cfg.accentColor};margin-bottom:4px}
  .hdr-ref{font-size:.95rem;font-weight:800;color:#fff;letter-spacing:.02em}
  .hdr-date{font-size:.7rem;color:rgba(255,255,255,.6);margin-top:3px}
  .status-bar{background:#f0fdf4;border-bottom:1px solid #bbf7d0;padding:11px 36px;display:flex;align-items:center;gap:10px}
  .status-dot{width:9px;height:9px;border-radius:50%;background:#16a34a;flex-shrink:0}
  .status-txt{font-size:.8rem;font-weight:700;color:#166534}
  .status-time{margin-left:auto;font-size:.75rem;color:#4ade80}
  .hero{padding:24px 36px 20px;border-bottom:1px solid #e8eaef;display:flex;align-items:flex-start;justify-content:space-between;gap:16px}
  .hero-lbl{font-size:.72rem;font-weight:600;text-transform:uppercase;letter-spacing:.08em;color:#5f6d89;margin-bottom:5px}
  .hero-val{font-size:1.9rem;font-weight:900;letter-spacing:-.03em;color:${cfg.brandColor}}
  .hero-cur{font-size:.9rem;font-weight:700;color:#5f6d89;margin-left:4px}
  .hero-sub{font-size:.75rem;color:#5f6d89;margin-top:4px}
  .route{display:flex;align-items:center;gap:10px}
  .route-box{background:#f5f8ff;border:1px solid #dbe2f0;border-radius:8px;padding:5px 12px;font-weight:700;color:${cfg.brandColor};font-size:.8rem;white-space:nowrap}
  .route-arr{color:${cfg.accentColor};font-size:1.05rem;font-weight:900}
  .sec{padding:22px 36px}
  .sec-title{font-size:.68rem;font-weight:800;letter-spacing:.1em;text-transform:uppercase;color:${cfg.accentColor};margin-bottom:12px;padding-bottom:7px;border-bottom:2px solid ${cfg.accentColor}22}
  table{width:100%;border-collapse:collapse}
  tr.e td{background:#f8faff}
  tr.o td{background:#fff}
  td{padding:8px 13px;border-bottom:1px solid #f0f1f6;vertical-align:top}
  .lbl{width:42%;font-size:.78rem;color:#5f6d89;font-weight:500}
  .val{font-size:.83rem;font-weight:700;color:#12203d}
  .val-ok{color:#16a34a}
  .val-ok::before{content:'✓ '}
  .val-warn{color:#d97706}
  .seal-row{padding:18px 36px;border-top:1px solid #e8eaef;display:flex;align-items:center;gap:16px}
  .seal{width:68px;height:68px;border-radius:50%;border:3px solid ${cfg.brandColor}22;display:flex;align-items:center;justify-content:center;flex-shrink:0}
  .seal-in{width:54px;height:54px;border-radius:50%;border:2px solid ${cfg.accentColor};display:flex;align-items:center;justify-content:center;flex-direction:column}
  .seal-chk{font-size:.95rem;color:${cfg.accentColor};font-weight:900}
  .seal-txt{font-size:.38rem;font-weight:900;letter-spacing:.06em;text-transform:uppercase;color:${cfg.brandColor};text-align:center;line-height:1.3}
  .seal-info{font-size:.73rem;color:#5f6d89;line-height:1.6}
  .seal-info strong{display:block;color:#12203d;font-size:.8rem;margin-bottom:2px}
  .ftr{background:#f5f8ff;border-top:1px solid #dbe2f0;padding:14px 36px;display:flex;align-items:flex-start;justify-content:space-between;gap:16px}
  .ftr-legal{font-size:.68rem;color:#9696a8;max-width:480px;line-height:1.6}
  .ftr-brand{text-align:right;flex-shrink:0}
  .ftr-name{font-size:.76rem;font-weight:800;color:${cfg.brandColor}}
  .ftr-web{font-size:.66rem;color:#9696a8;margin-top:2px}
  @media print{body{background:#fff;padding:0}.page{box-shadow:none;border-radius:0}}
  </style></head><body><div class="page">
  <div class="hdr">
    <div style="display:flex;align-items:center;gap:10px">
      <div class="hdr-mark">N</div>
      <div><div class="hdr-name">NEO<span>BTE</span></div><div class="hdr-legal">${cfg.bankLegalName}</div></div>
    </div>
    <div class="hdr-right">
      <div class="hdr-type">${title}</div>
      <div class="hdr-ref">${ref}</div>
      <div class="hdr-date">${dateStr}</div>
    </div>
  </div>
  <div class="status-bar">
    <div class="status-dot"></div>
    <span class="status-txt">Document officiel NEO BTE</span>
    <span class="status-time">${timeStr}</span>
  </div>
  ${body}
  <div class="seal-row">
    <div class="seal"><div class="seal-in"><div class="seal-chk">✓</div><div class="seal-txt">VALIDÉ<br>BTE</div></div></div>
    <div class="seal-info"><strong>Document authentique</strong>Référence : <strong>${ref}</strong> · Généré le ${dateStr} à ${timeStr}<br>Ce document est produit automatiquement par le système NEO BTE.</div>
  </div>
  <div class="ftr">
    <div class="ftr-legal">${cfg.legalNote}</div>
    <div class="ftr-brand"><div class="ftr-name">${cfg.bankName}</div><div class="ftr-web">${cfg.bankWebsite}</div><div class="ftr-web">${cfg.bankAddress}</div></div>
  </div>
  </div></body></html>`;
    }

    private rows(pairs: [string, string, string?][]): string {
        return pairs.map(([lbl, val, cls], i) =>
            `<tr class="${i % 2 === 0 ? 'e' : 'o'}"><td class="lbl">${lbl}</td><td class="val${cls ? ' ' + cls : ''}">${val}</td></tr>`
        ).join('');
    }

    // ── Virement HTML ─────────────────────────────────────────────────────────

    private buildVirementHtml(v: Virement, profile: ClientProfile | null, mode: 'externe' | 'interne', cfg: ContratConfig): string {
        const date = new Date(v.dateDeVirement);
        const dateStr = this.fmtDate(v.dateDeVirement, { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
        const timeStr = date.toLocaleTimeString('fr-TN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        const ref = `VIR-${String(v.idVirement).padStart(8, '0')}`;
        const senderFull = this.fullName(profile) !== '—' ? this.fullName(profile) : (v.senderName || '—');

        const tableRows = this.rows([
            ['Référence', ref],
            ['Date', dateStr],
            ['Heure', timeStr],
            ['Type', mode === 'externe' ? 'Virement externe' : 'Virement interne'],
            ['Compte source', `#${v.compteSourceId}`],
            mode === 'externe'
                ? ['Bénéficiaire', v.recipientName || '—']
                : ['Compte destination', `#${v.compteDestinationId}`],
            ['Montant transféré', `${this.fmt(v.montant)} TND`],
            ['Frais bancaires', v.frais ? `${this.fmt(v.frais)} TND` : 'Aucun'],
            ['Total débité', `${this.fmt(v.totalDebite ?? v.montant)} TND`],
            ['Taux de frais', v.tauxFrais ? `${(v.tauxFrais * 100).toFixed(2)} %` : '0.00 %'],
            ['Donneur d\'ordre', senderFull],
            ['Statut', 'Exécuté', 'val-ok'],
        ]);

        const dest = mode === 'externe' ? (v.recipientName || 'Bénéficiaire') : `#${v.compteDestinationId}`;

        const body = `
  <div class="hero">
    <div>
      <div class="hero-lbl">Montant transféré</div>
      <div class="hero-val">${this.fmt(v.montant)}<span class="hero-cur">TND</span></div>
      ${v.frais ? `<div class="hero-sub">+ ${this.fmt(v.frais)} TND de frais bancaires</div>` : ''}
    </div>
    <div class="route">
      <div class="route-box">#${v.compteSourceId}</div>
      <div class="route-arr">→</div>
      <div class="route-box">${dest}</div>
    </div>
  </div>
  <div class="sec">
    <div class="sec-title">Détails de la transaction</div>
    <table><tbody>${tableRows}</tbody></table>
  </div>`;

        return this.shell(cfg, 'Contrat de virement', ref, dateStr, timeStr, body);
    }

    // ── Loan HTML ─────────────────────────────────────────────────────────────

    private buildLoanHtml(loan: Loan, profile: ClientProfile | null, cfg: ContratConfig): string {
        const dateStr = this.fmtDate(loan.dateCreation, { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
        const timeStr = new Date(loan.dateCreation).toLocaleTimeString('fr-TN', { hour: '2-digit', minute: '2-digit' });
        const ref = `PRET-${String(loan.id).padStart(8, '0')}`;
        const typeLabel = LOAN_TYPE_LABELS[loan.type] ?? loan.type;

        const statutLabel: Record<string, string> = {
            PENDING_APPROVAL: 'En attente d\'approbation',
            APPROVED: 'Approuvé',
            ACTIVE: 'Actif',
            LATE: 'En retard',
            DEFAULT: 'Défaut',
            PAID_OFF: 'Remboursé',
            REJECTED: 'Rejeté',
        };

        const tableRows = this.rows([
            ['Référence', ref],
            ['Date de demande', dateStr],
            ['Produit', loan.productNom ?? typeLabel],
            ['Type de prêt', typeLabel],
            ['Montant emprunté', `${this.fmt(loan.montant)} TND`],
            ['Durée', `${loan.dureeEnMois} mois`],
            ['Taux annuel', `${(loan.tauxAnnuel * 100).toFixed(2)} %`],
            ['Mensualité', `${this.fmt(loan.mensualite)} TND / mois`],
            ['Total dû', `${this.fmt(loan.totalDu)} TND`],
            ['Total intérêts', `${this.fmt(loan.totalInteret)} TND`],
            ['Compte crédité', `#${loan.compteDestinationId}`],
            ['Compte prélevé', `#${loan.comptePrelevementId}`],
            loan.dateApprobation ? ['Date d\'approbation', this.fmtDate(loan.dateApprobation)] : ['Date d\'approbation', '—'],
            loan.dateDisbursement ? ['Date de déblocage', this.fmtDate(loan.dateDisbursement)] : null,
            ['Emprunteur', this.fullName(profile)],
            ['Statut', statutLabel[loan.statut] ?? loan.statut, loan.statut === 'ACTIVE' || loan.statut === 'PAID_OFF' || loan.statut === 'APPROVED' ? 'val-ok' : loan.statut === 'LATE' || loan.statut === 'DEFAULT' ? 'val-warn' : undefined],
            loan.motifRejet ? ['Motif de refus', loan.motifRejet] : null,
        ].filter(Boolean) as [string, string, string?][]);

        const body = `
  <div class="hero">
    <div>
      <div class="hero-lbl">Montant emprunté</div>
      <div class="hero-val">${this.fmt(loan.montant)}<span class="hero-cur">TND</span></div>
      <div class="hero-sub">Mensualité : ${this.fmt(loan.mensualite)} TND · ${loan.dureeEnMois} mois</div>
    </div>
    <div class="route">
      <div class="route-box">${typeLabel}</div>
      <div class="route-arr">→</div>
      <div class="route-box">#${loan.compteDestinationId}</div>
    </div>
  </div>
  <div class="sec">
    <div class="sec-title">Conditions du contrat de prêt</div>
    <table><tbody>${tableRows}</tbody></table>
  </div>`;

        return this.shell(cfg, 'Contrat de prêt', ref, dateStr, timeStr, body);
    }

    // ── Placement HTML ────────────────────────────────────────────────────────

    private buildPlacementHtml(inv: Investment, profile: ClientProfile | null, cfg: ContratConfig): string {
        const dateStr = this.fmtDate(inv.dateDebut, { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
        const timeStr = new Date(inv.dateDebut).toLocaleTimeString('fr-TN', { hour: '2-digit', minute: '2-digit' });
        const ref = `PLAC-${String(inv.id).padStart(8, '0')}`;

        const statutLabel: Record<string, string> = {
            ACTIVE: 'Actif', COMPLETED: 'Complété', CANCELLED: 'Annulé',
        };

        const tableRows = this.rows([
            ['Référence', ref],
            ['Date de souscription', dateStr],
            ['Plan', inv.planNom],
            ['Montant placé', `${this.fmt(inv.montant)} TND`],
            ['Durée', `${inv.dureeEnMois} mois`],
            ['Taux annuel', `${(inv.tauxAnnuel * 100).toFixed(2)} %`],
            ['Intérêt attendu à l\'échéance', `+${this.fmt(inv.interetAttendu)} TND`],
            ['Capital + intérêts', `${this.fmt(inv.montant + inv.interetAttendu)} TND`],
            ['Date de début', this.fmtDate(inv.dateDebut)],
            ['Date d\'échéance', this.fmtDate(inv.dateEcheance)],
            ['Compte source', `#${inv.compteId}`],
            ['Souscripteur', this.fullName(profile)],
            ['Statut', statutLabel[inv.statut] ?? inv.statut, inv.statut === 'ACTIVE' || inv.statut === 'COMPLETED' ? 'val-ok' : undefined],
        ]);

        const body = `
  <div class="hero">
    <div>
      <div class="hero-lbl">Montant placé</div>
      <div class="hero-val">${this.fmt(inv.montant)}<span class="hero-cur">TND</span></div>
      <div class="hero-sub">Rendement attendu : +${this.fmt(inv.interetAttendu)} TND · ${(inv.tauxAnnuel * 100).toFixed(2)}% / an</div>
    </div>
    <div class="route">
      <div class="route-box">#${inv.compteId}</div>
      <div class="route-arr">→</div>
      <div class="route-box">${inv.planNom}</div>
    </div>
  </div>
  <div class="sec">
    <div class="sec-title">Conditions du contrat de placement</div>
    <table><tbody>${tableRows}</tbody></table>
  </div>`;

        return this.shell(cfg, 'Contrat de placement', ref, dateStr, timeStr, body);
    }
}