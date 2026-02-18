export class User {
  idUtilisateur?: number;
  email!: string;
  nom!: string;
  prenom!: string;
  age?: number;
  adresse?: string;
  job?: string;
  genre?: string;
  solde?: number;
  role?: 'ADMIN' | 'CLIENT';
}