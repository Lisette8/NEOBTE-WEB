export interface Virement {
  idVirement: number;
  compteSourceId: number;
  compteDestinationId: number;
  montant: number;
  dateDeVirement: string;
  idempotencyKey?: string; //? is optional and ! is undifined 
}