export interface TransferConstraints {
  feeRate: number;
  largeTransferThreshold: number | null;
  dailyAmountLimit: number | null;
  dailyCountLimit: number | null;
}

