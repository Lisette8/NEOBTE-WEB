export interface Support {
  idSupport: number;
  sujet: string;
  message: string;
  reponseAdmin?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  dateCreation: string;
  clientEmail?: string;
  guestEmail?: string;
  guestName?: string;
  guest: boolean;
}