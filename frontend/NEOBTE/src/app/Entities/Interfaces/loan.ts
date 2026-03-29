export type LoanStatut = 'PENDING_APPROVAL' | 'APPROVED' | 'ACTIVE' | 'LATE' | 'DEFAULT' | 'PAID_OFF' | 'REJECTED';
export type RepaymentStatut = 'PENDING' | 'PAID' | 'LATE' | 'FAILED' | 'WAIVED';
export type LoanType = 'PERSONNEL' | 'IMMOBILIER' | 'AUTO' | 'PROFESSIONNEL';

export interface LoanProduct {
    id: number;
    nom: string;
    description: string;
    type: LoanType;
    dureeEnMois: number;
    tauxAnnuel: number;
    montantMin: number;
    montantMax: number;
    gracePeriodDays: number;
    penaltyRate: number;
    penaltyFixedFee: number;
    defaultThreshold: number;
    actif: boolean;
    exampleMensualite: number;
}

export interface LoanRepayment {
    id: number;
    installmentNumber: number;
    dateDue: string;
    montantDu: number;
    principalPortion: number;
    interetPortion: number;
    penalite: number;
    montantPaye: number;
    statut: RepaymentStatut;
    datePaiement?: string;
    penaltyApplied: boolean;
}

export interface Loan {
    id: number;
    compteDestinationId: number;
    comptePrelevementId: number;
    productId: number;
    productNom: string;
    type: LoanType;
    montant: number;
    tauxAnnuel: number;
    dureeEnMois: number;
    mensualite: number;
    totalDu: number;
    totalInteret: number;
    totalRembourse: number;
    totalPenalites: number;
    resteADu: number;
    missedPayments: number;
    statut: LoanStatut;
    motifRejet?: string;
    adminNote?: string;
    dateCreation: string;
    dateApprobation?: string;
    dateDisbursement?: string;
    dateCloture?: string;
    progressPct: number;
    repayments: LoanRepayment[];
}

export interface LoanSimulation {
    montant: number;
    tauxAnnuel: number;
    dureeEnMois: number;
    mensualite: number;
    totalDu: number;
    totalInteret: number;
}

export interface LoanRequestDTO {
    productId: number;
    compteDestinationId: number;
    comptePrelevementId: number;
    montant: number;
    motif?: string;
}

export interface LoanProductFormDTO {
    nom: string;
    description: string;
    type: LoanType;
    dureeEnMois: number;
    tauxAnnuel: number;
    montantMin: number;
    montantMax: number;
    gracePeriodDays: number;
    penaltyRate: number;
    penaltyFixedFee: number;
    defaultThreshold: number;
    actif: boolean;
}

export const LOAN_TYPE_LABELS: Record<LoanType, string> = {
    PERSONNEL: 'Personnel',
    IMMOBILIER: 'Immobilier',
    AUTO: 'Auto',
    PROFESSIONNEL: 'Professionnel',
};

export const LOAN_STATUT_LABELS: Record<LoanStatut, { label: string; color: string }> = {
    PENDING_APPROVAL: { label: 'En attente', color: '#f59e0b' },
    APPROVED: { label: 'Approuvé', color: '#3b82f6' },
    ACTIVE: { label: 'Actif', color: '#10b981' },
    LATE: { label: 'En retard', color: '#f97316' },
    DEFAULT: { label: 'Défaut', color: '#ef4444' },
    PAID_OFF: { label: 'Remboursé', color: '#6366f1' },
    REJECTED: { label: 'Refusé', color: '#6b7280' },
};