import { Injectable } from "@angular/core";
import { ClientProfile } from "../Entities/Interfaces/client-profile";
import { Virement } from "../Entities/Interfaces/virement";

export interface ContratConfig {
    /** Bank name shown in header */
    bankName: string;
    /** Bank full legal name */
    bankLegalName: string;
    /** Bank address line */
    bankAddress: string;
    /** Bank website */
    bankWebsite: string;
    /** Primary brand color (hex) */
    brandColor: string;
    /** Secondary/gold accent color (hex) */
    accentColor: string;
    /** Footer legal note */
    legalNote: string;
}

/** Default BTE config — swap any field to match the real bank template */
export const DEFAULT_CONTRAT_CONFIG: ContratConfig = {
    bankName: 'NEO BTE',
    bankLegalName: 'Banque de Tunisie et des Émirats',
    bankAddress: 'Boulevard Beji Caid Essebsi – Centre Urbain Nord – 1082 Tunis',
    bankWebsite: 'www.bte.com.tn',
    brandColor: '#0d2b77',
    accentColor: '#ba9553',
    legalNote:
        'Ce document constitue un justificatif de virement émis par NEO BTE. ' +
        'Il est généré automatiquement et ne nécessite pas de signature. ' +
        'Conservez-le comme preuve de transaction. ' +
        'Pour toute contestation, contactez votre agence BTE dans les 30 jours.',
};

@Injectable({ providedIn: 'root' })
export class ContratVirementService {

    /**
     * Opens a new window with the contract HTML and triggers print dialog.
     * The user can save as PDF from the browser print dialog.
     */
    print(
        virement: Virement,
        profile: ClientProfile | null,
        mode: 'externe' | 'interne',
        config: ContratConfig = DEFAULT_CONTRAT_CONFIG
    ): void {
        const html = this.buildHtml(virement, profile, mode, config);
        const win = window.open('', '_blank', 'width=860,height=1100');
        if (!win) return;
        win.document.open();
        win.document.write(html);
        win.document.close();
        // Give time for fonts/styles to load before print dialog
        win.onload = () => {
            setTimeout(() => {
                win.focus();
                win.print();
            }, 400);
        };
    }

    private buildHtml(
        v: Virement,
        profile: ClientProfile | null,
        mode: 'externe' | 'interne',
        cfg: ContratConfig
    ): string {
        const date = new Date(v.dateDeVirement);
        const dateFormatted = date.toLocaleDateString('fr-TN', {
            weekday: 'long', day: '2-digit', month: 'long', year: 'numeric'
        });
        const timeFormatted = date.toLocaleTimeString('fr-TN', {
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
        const ref = `VIR-${String(v.idVirement).padStart(8, '0')}`;
        const senderFull = profile
            ? `${profile.prenom} ${profile.nom}`.trim()
            : (v.senderName || '—');
        const fmt = (n: number) =>
            n.toLocaleString('fr-TN', { minimumFractionDigits: 3, maximumFractionDigits: 3 });

        const rows = [
            ['Référence', ref],
            ['Date', `${dateFormatted}`],
            ['Heure', timeFormatted],
            ['Type', mode === 'externe' ? 'Virement externe' : 'Virement interne'],
            ['Compte source', `#${v.compteSourceId}`],
            mode === 'externe'
                ? ['Bénéficiaire', v.recipientName || '—']
                : ['Compte destination', `#${v.compteDestinationId}`],
            ['Montant transféré', `${fmt(v.montant)} TND`],
            ['Frais bancaires', v.frais ? `${fmt(v.frais)} TND` : 'Aucun'],
            ['Total débité', `${fmt(v.totalDebite ?? v.montant)} TND`],
            ['Taux de frais', v.tauxFrais ? `${(v.tauxFrais * 100).toFixed(2)} %` : '0.00 %'],
            ['Donneur d\'ordre', senderFull],
            ['Statut', 'Exécuté'],
        ];

        const rowsHtml = rows.map(([label, value], i) => `
        <tr class="${i % 2 === 0 ? 'even' : 'odd'}">
          <td class="label">${label}</td>
          <td class="value ${label === 'Statut' ? 'value-status' : ''}">${value}</td>
        </tr>
      `).join('');

        return `<!DOCTYPE html>
  <html lang="fr">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contrat de virement — ${ref}</title>
    <style>
      /* ── Reset ── */
      *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
   
      body {
        font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Arial, sans-serif;
        background: #f4f6fb;
        color: #12203d;
        font-size: 13px;
        line-height: 1.5;
        padding: 32px 24px;
      }
   
      /* ── Page wrapper ── */
      .page {
        max-width: 720px;
        margin: 0 auto;
        background: #fff;
        border-radius: 16px;
        box-shadow: 0 4px 32px rgba(13,43,119,0.12);
        overflow: hidden;
      }
   
      /* ── Header ── */
      .header {
        background: ${cfg.brandColor};
        padding: 28px 36px 24px;
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
      }
   
      .header-left {}
   
      .bank-logo {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;
      }
   
      .bank-mark {
        width: 40px;
        height: 40px;
        border-radius: 11px;
        background: rgba(255,255,255,0.15);
        border: 1.5px solid rgba(255,255,255,0.25);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        color: #fff;
        font-weight: 900;
        letter-spacing: -1px;
      }
   
      .bank-name {
        font-size: 1.3rem;
        font-weight: 800;
        color: #fff;
        letter-spacing: -0.02em;
      }
   
      .bank-name span { color: ${cfg.accentColor}; }
   
      .bank-legal {
        font-size: 0.72rem;
        color: rgba(255,255,255,0.65);
        margin-top: 2px;
      }
   
      .doc-title {
        text-align: right;
      }
   
      .doc-type {
        font-size: 0.7rem;
        font-weight: 700;
        letter-spacing: 0.1em;
        text-transform: uppercase;
        color: ${cfg.accentColor};
        margin-bottom: 4px;
      }
   
      .doc-ref {
        font-size: 1rem;
        font-weight: 800;
        color: #fff;
        letter-spacing: 0.02em;
      }
   
      .doc-date {
        font-size: 0.72rem;
        color: rgba(255,255,255,0.6);
        margin-top: 4px;
      }
   
      /* ── Status banner ── */
      .status-banner {
        background: #f0fdf4;
        border-bottom: 1px solid #bbf7d0;
        padding: 12px 36px;
        display: flex;
        align-items: center;
        gap: 10px;
      }
   
      .status-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: #16a34a;
        flex-shrink: 0;
      }
   
      .status-text {
        font-size: 0.82rem;
        font-weight: 700;
        color: #166534;
      }
   
      .status-sub {
        font-size: 0.78rem;
        color: #4ade80;
        margin-left: auto;
      }
   
      /* ── Amount hero ── */
      .amount-hero {
        padding: 28px 36px 22px;
        border-bottom: 1px solid #e8eaef;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
      }
   
      .amount-label {
        font-size: 0.75rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: #5f6d89;
        margin-bottom: 6px;
      }
   
      .amount-value {
        font-size: 2rem;
        font-weight: 900;
        letter-spacing: -0.03em;
        color: ${cfg.brandColor};
      }
   
      .amount-currency {
        font-size: 1rem;
        font-weight: 700;
        color: #5f6d89;
        margin-left: 4px;
      }
   
      .amount-route {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 0.82rem;
        color: #5f6d89;
      }
   
      .route-acct {
        background: #f5f8ff;
        border: 1px solid #dbe2f0;
        border-radius: 8px;
        padding: 6px 12px;
        font-weight: 700;
        color: ${cfg.brandColor};
        font-size: 0.82rem;
      }
   
      .route-arrow {
        color: ${cfg.accentColor};
        font-size: 1.1rem;
        font-weight: 900;
      }
   
      /* ── Details table ── */
      .section {
        padding: 24px 36px;
      }
   
      .section-title {
        font-size: 0.7rem;
        font-weight: 800;
        letter-spacing: 0.1em;
        text-transform: uppercase;
        color: ${cfg.accentColor};
        margin-bottom: 14px;
        padding-bottom: 8px;
        border-bottom: 2px solid ${cfg.accentColor}22;
      }
   
      table {
        width: 100%;
        border-collapse: collapse;
      }
   
      tr.even td { background: #f8faff; }
      tr.odd  td { background: #fff; }
   
      td {
        padding: 9px 14px;
        border-bottom: 1px solid #f0f1f6;
        vertical-align: top;
      }
   
      td:first-child { border-radius: 4px 0 0 4px; }
      td:last-child  { border-radius: 0 4px 4px 0; }
   
      .label {
        width: 42%;
        font-size: 0.8rem;
        color: #5f6d89;
        font-weight: 500;
      }
   
      .value {
        font-size: 0.85rem;
        font-weight: 700;
        color: #12203d;
      }
   
      .value-status {
        color: #16a34a;
      }
   
      .value-status::before {
        content: '✓ ';
      }
   
      /* ── Watermark / seal ── */
      .seal-row {
        padding: 20px 36px;
        border-top: 1px solid #e8eaef;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
      }
   
      .seal {
        width: 72px;
        height: 72px;
        border-radius: 50%;
        border: 3px solid ${cfg.brandColor}22;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        flex-shrink: 0;
        position: relative;
      }
   
      .seal-inner {
        width: 58px;
        height: 58px;
        border-radius: 50%;
        border: 2px solid ${cfg.accentColor};
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
      }
   
      .seal-text {
        font-size: 0.42rem;
        font-weight: 900;
        letter-spacing: 0.06em;
        text-transform: uppercase;
        color: ${cfg.brandColor};
        text-align: center;
        line-height: 1.3;
      }
   
      .seal-checkmark {
        font-size: 1rem;
        color: ${cfg.accentColor};
        font-weight: 900;
      }
   
      .seal-info {
        font-size: 0.75rem;
        color: #5f6d89;
        line-height: 1.6;
      }
   
      .seal-info strong {
        display: block;
        color: #12203d;
        font-size: 0.82rem;
        margin-bottom: 2px;
      }
   
      /* ── Footer ── */
      .footer {
        background: #f5f8ff;
        border-top: 1px solid #dbe2f0;
        padding: 16px 36px;
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
      }
   
      .footer-legal {
        font-size: 0.7rem;
        color: #9696a8;
        max-width: 480px;
        line-height: 1.6;
      }
   
      .footer-brand {
        text-align: right;
        flex-shrink: 0;
      }
   
      .footer-brand-name {
        font-size: 0.78rem;
        font-weight: 800;
        color: ${cfg.brandColor};
      }
   
      .footer-web {
        font-size: 0.68rem;
        color: #9696a8;
        margin-top: 2px;
      }
   
      /* ── Print overrides ── */
      @media print {
        body { background: #fff; padding: 0; }
        .page { box-shadow: none; border-radius: 0; }
      }
    </style>
  </head>
  <body>
    <div class="page">
   
      <!-- Header -->
      <div class="header">
        <div class="header-left">
          <div class="bank-logo">
            <div class="bank-mark">N</div>
            <div>
              <div class="bank-name">NEO<span>BTE</span></div>
              <div class="bank-legal">${cfg.bankLegalName}</div>
            </div>
          </div>
        </div>
        <div class="doc-title">
          <div class="doc-type">Contrat de virement</div>
          <div class="doc-ref">${ref}</div>
          <div class="doc-date">${dateFormatted}</div>
        </div>
      </div>
   
      <!-- Status banner -->
      <div class="status-banner">
        <div class="status-dot"></div>
        <span class="status-text">Virement exécuté avec succès</span>
        <span class="status-sub">${timeFormatted}</span>
      </div>
   
      <!-- Amount hero -->
      <div class="amount-hero">
        <div>
          <div class="amount-label">Montant transféré</div>
          <div class="amount-value">${fmt(v.montant)}<span class="amount-currency">TND</span></div>
          ${v.frais ? `<div style="font-size:0.78rem;color:#5f6d89;margin-top:4px;">+ ${fmt(v.frais)} TND de frais bancaires</div>` : ''}
        </div>
        <div class="amount-route">
          <div class="route-acct">#${v.compteSourceId}</div>
          <div class="route-arrow">→</div>
          <div class="route-acct">${mode === 'externe' ? (v.recipientName || 'Bénéficiaire') : `#${v.compteDestinationId}`}</div>
        </div>
      </div>
   
      <!-- Details -->
      <div class="section">
        <div class="section-title">Détails de la transaction</div>
        <table>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>
      </div>
   
      <!-- Seal -->
      <div class="seal-row">
        <div class="seal">
          <div class="seal-inner">
            <div class="seal-checkmark">✓</div>
            <div class="seal-text">VALIDÉ<br>BTE</div>
          </div>
        </div>
        <div class="seal-info">
          <strong>Document authentique</strong>
          Ce contrat a été généré automatiquement par le système NEO BTE.<br>
          Référence : <strong>${ref}</strong> · Horodatage : ${dateFormatted} à ${timeFormatted}<br>
          Donneur d'ordre : <strong>${senderFull}</strong>
        </div>
      </div>
   
      <!-- Footer -->
      <div class="footer">
        <div class="footer-legal">${cfg.legalNote}</div>
        <div class="footer-brand">
          <div class="footer-brand-name">${cfg.bankName}</div>
          <div class="footer-web">${cfg.bankWebsite}</div>
          <div class="footer-web">${cfg.bankAddress}</div>
        </div>
      </div>
   
    </div>
  </body>
  </html>`;
    }
}