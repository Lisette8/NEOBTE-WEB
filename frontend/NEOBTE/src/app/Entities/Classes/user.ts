export class User {
  idUtilisateur?: number;
  email!: string;
  username!: string;
  nom!: string;
  prenom!: string;
  cin?: string;
  telephone?: string;
  adresse?: string;
  codePostal?: string;
  pays?: string;
  dateNaissance?: string;
  job?: string;
  genre?: 'HOMME' | 'FEMME';
  role?: 'ADMIN' | 'CLIENT';
  dateCreationCompte?: string;
  totalSolde?: number;
}