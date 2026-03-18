export interface RecipientPreview {
    displayName: string;
    maskedIdentifier: string;
    primaryCompteId: number;
    primaryCompteType: string;
    found: boolean;
    photoUrl?: string | null;
    feeRate: number;
    estimatedFee: number | null;
 
    // Fraud limits — returned by backend so UI can warn before submitting
    largeTransferThreshold: number | null;
    dailyAmountLimit: number | null;
    dailyCountLimit: number | null;
}
