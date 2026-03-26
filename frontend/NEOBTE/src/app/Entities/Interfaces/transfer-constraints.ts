export interface TransferConstraints {
  feeRate: number;
  largeTransferThreshold: number | null;
  dailyAmountLimit: number | null;
  dailyCountLimit: number | null;
  monthlyCountLimit: number | null;
  canSendExternal: boolean;
  accountTypePurpose: string | null;
  accountTypeLabel: string | null;
}
