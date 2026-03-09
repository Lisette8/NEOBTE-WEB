export interface Virement {
  idVirement: number;
  compteSourceId: number;
  compteDestinationId: number;
  montant: number;
  dateDeVirement: string;

}