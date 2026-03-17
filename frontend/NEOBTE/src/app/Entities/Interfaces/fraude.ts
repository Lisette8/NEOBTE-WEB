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
    dailyCountLimit: number;
    dailyAmountLimit: number;
    largeTransferThreshold: number;
    rapidSuccessionCount: number;
    rapidSuccessionMinutes: number;
    suspiciousHourStart: number;
    suspiciousHourEnd: number;
    emailAlertsEnabled: boolean;
  }
   
  export type FraudeAlertType =
    | 'SUSPICIOUS_HOUR'
    | 'DAILY_COUNT_EXCEEDED'
    | 'DAILY_AMOUNT_EXCEEDED'
    | 'RAPID_SUCCESSION'
    | 'LARGE_SINGLE_TRANSFER';
   
  export type FraudeSeverity = 'LOW' | 'MEDIUM' | 'HIGH';
  export type FraudeStatut   = 'OPEN' | 'REVIEWED' | 'DISMISSED';