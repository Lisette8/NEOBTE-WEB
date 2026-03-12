export interface UserCreateDTO {
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
  motDePasse: string;
  role?: 'ADMIN' | 'CLIENT';
}