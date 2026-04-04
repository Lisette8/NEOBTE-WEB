export type SupportStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type SupportCategorie = 'VIREMENT' | 'COMPTE' | 'CARTE' | 'PRET' | 'PLACEMENT' | 'SECURITE' | 'AUTRE';
export type SupportPriorite = 'NORMALE' | 'URGENTE';

export interface Support {
  idSupport: number;
  sujet: string;
  message: string;
  reponseAdmin?: string;
  status: SupportStatus;
  categorie: SupportCategorie;
  priorite: SupportPriorite;
  dateCreation: string;
  clientEmail?: string;
  guestEmail?: string;
  guestName?: string;
  guest: boolean;
}