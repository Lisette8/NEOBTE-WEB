export type NotificationType =
  | 'ACTUALITE_CREATED'
  | 'ACTUALITE_UPDATED'
  | 'TRANSFER_SENT'
  | 'TRANSFER_RECEIVED'
  | 'PASSWORD_CHANGED'
  | 'REFERRAL_REWARD'
  | 'INVESTMENT_CREATED'
  | 'INVESTMENT_MATURED';

export interface ClientNotification {
  id: number;
  type: NotificationType;
  titre: string;
  message: string;
  lien?: string | null;
  lu: boolean;
  dateCreation: string;
}
