export interface UserListDTO {
  id: number;
  email: string;
  username: string;
  nom: string;
  prenom: string;
  cin?: string;
  telephone?: string;
  adresse?: string;
  codePostal?: string;
  pays?: string;
  dateNaissance?: string;
  job?: string;
  genre?: string;
  role: 'ADMIN' | 'CLIENT';
  dateCreationCompte?: string;
  totalSolde?: number;
}