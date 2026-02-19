export interface UserListDTO {
  idUtilisateur: number;
  email: string;
  nom: string;
  prenom: string;
  role: string;
  solde: number;
  age?: number;
  adresse?: string;
  job?: string;
  genre?: 'HOMME' | 'FEMME';
}
