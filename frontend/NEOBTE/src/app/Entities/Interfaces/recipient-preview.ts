export interface RecipientPreview {
    displayName: string;
    maskedIdentifier: string;
    primaryCompteId: number;
    primaryCompteType: string;
    found: boolean;
    feeRate: number;
    estimatedFee: number | null;
}