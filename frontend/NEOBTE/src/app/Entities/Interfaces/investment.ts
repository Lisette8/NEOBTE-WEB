export interface MonthlyEarning {
    moisNumero: number;
    mois: string;         // "2025-03"
    moisLabel: string;    // "Mars 2025"
    montantInteret: number;
    accrued: boolean;
}

export interface InvestmentPlan {
    id: number;
    nom: string;
    description: string;
    dureeEnMois: number;
    tauxAnnuel: number;
    montantMin: number;
    montantMax: number;
    actif: boolean;
}

export interface Investment {
    id: number;
    compteId: number;
    planId: number;
    planNom: string;
    montant: number;
    tauxAnnuel: number;
    dureeEnMois: number;
    interetAttendu: number;
    interetVerse: number;
    dateDebut: string;
    dateEcheance: string;
    dateCloture?: string;
    statut: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
    progressPct: number;
    currentValue: number;
    daysRemaining: number;
    totalAccrued: number;
    monthlyBreakdown: MonthlyEarning[];
}

export interface InvestmentCreateDTO {
    planId: number;
    compteId: number;
    montant: number;
}

export interface InvestmentPlanFormDTO {
    nom: string;
    description: string;
    dureeEnMois: number;
    tauxAnnuel: number;
    montantMin: number;
    montantMax: number;
    actif: boolean;
}