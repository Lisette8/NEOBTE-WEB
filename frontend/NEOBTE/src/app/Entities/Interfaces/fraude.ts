export interface FraudeAlerte {
  id: number;
  type: FraudeAlertType;
  severity: FraudeSeverity;
  statut: FraudeStatut;
  description: string;
  adminNote?: string;
  utilisateurId: number;
  utilisateurNom: string;
  utilisateurEmail: string;
  virementId?: number;
  virementMontant?: number;
  dateAlerte: string;
  dateRevue?: string;
}

export interface FraudeConfig {
  // Fraud detection
  largeTransferThreshold: number;
  rapidSuccessionCount: number;
  rapidSuccessionMinutes: number;
  suspiciousHourStart: number;
  suspiciousHourEnd: number;
  emailAlertsEnabled: boolean;

  // Fee rates (admin-editable)
  courantFeeRate: number;
  epargneFeeRate: number;
  professionnelFeeRate: number;

  // COURANT limits
  courantDailyAmountLimit: number;
  courantDailyCountLimit: number;
  courantMonthlyCountLimit: number;
  courantMaxTransfer: number;

  // EPARGNE limits
  epargneDailyAmountLimit: number;
  epargneDailyCountLimit: number;
  epargneMonthlyCountLimit: number;
  epargneMaxTransfer: number;

  // PROFESSIONNEL limits
  professionnelDailyAmountLimit: number;
  professionnelDailyCountLimit: number;
  professionnelMonthlyCountLimit: number;
  professionnelMaxTransfer: number;
}

export type FraudeAlertType =
  | 'SUSPICIOUS_HOUR' | 'DAILY_COUNT_EXCEEDED'
  | 'DAILY_AMOUNT_EXCEEDED' | 'RAPID_SUCCESSION' | 'LARGE_SINGLE_TRANSFER';

export type FraudeSeverity = 'LOW' | 'MEDIUM' | 'HIGH';
export type FraudeStatut = 'OPEN' | 'REVIEWED' | 'DISMISSED';