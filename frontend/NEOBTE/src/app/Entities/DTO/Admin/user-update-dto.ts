export interface UserUpdateDTO {
  nom?: string;
  prenom?: string;
  telephone?: string;
  adresse?: string;
  codePostal?: string;
  pays?: string;
  dateNaissance?: string;
  job?: string;
  genre?: string;
  motDePasse?: string;
  role?: 'ADMIN' | 'CLIENT';
}