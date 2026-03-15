
export interface RibCompte {
  idCompte: number;
  typeCompte: string;
  rib: string;
  solde: number;
  statutCompte: string;
  isPrimary: boolean;
}
 
export interface Rib {
  nomComplet: string;
  email: string;
  telephone: string;
  comptes: RibCompte[];
}