export interface Compte {
    idCompte: number;
    solde: number;
    typeCompte: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL';
    statutCompte: string;
    utilisateurId: number;
    dateSuppressionPrevue?: string;
}

// Per-type display metadata (mirrors AccountTypePolicy on the backend)
export const ACCOUNT_TYPE_META: Record<string, {
    label: string;
    purpose: string;
    icon: string;
    color: string;
    canSendExternal: boolean;
    interestRate: number;
    maxTransfer: number;
}> = {
    COURANT: {
        label: 'Compte Chèque',
        purpose: 'Opérations quotidiennes — paiements, virements, retraits.',
        icon: '💳',
        color: '#3b82f6',
        canSendExternal: true,
        interestRate: 0,
        maxTransfer: 5000,
    },
    EPARGNE: {
        label: 'Compte Épargne',
        purpose: 'Épargne rémunérée. Retraits limités pour favoriser l\'accumulation.',
        icon: '🏦',
        color: '#10b981',
        canSendExternal: false,
        interestRate: 4.5,
        maxTransfer: 1000,
    },
    PROFESSIONNEL: {
        label: 'Compte Professionnel',
        purpose: 'Activité professionnelle à fort volume. Limites élevées.',
        icon: '🏢',
        color: '#8b5cf6',
        canSendExternal: true,
        interestRate: 0,
        maxTransfer: 50000,
    },
};