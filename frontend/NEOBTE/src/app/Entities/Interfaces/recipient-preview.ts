export interface RecipientPreview {
    displayName: string;
    maskedIdentifier: string;
    primaryCompteId: number;
    primaryCompteType: string;
    found: boolean;
    photoUrl?: string | null;
    feeRate: number;
    estimatedFee: number | null;

    // Sender account-type policy limits
    largeTransferThreshold: number | null;
    dailyAmountLimit: number | null;
    dailyCountLimit: number | null;
    monthlyCountLimit: number | null;
    canSendExternal: boolean;
}