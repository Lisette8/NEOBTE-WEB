export interface Virement {
  idVirement: number;
  compteSourceId: number;
  compteDestinationId: number;
  recipientName: string;
  senderName: string;
  montant: number;
  frais: number;
  totalDebite: number;
  tauxFrais: number;
  dateDeVirement: string;
  idempotencyKey?: string; //? is optional and ! is undifined 
}