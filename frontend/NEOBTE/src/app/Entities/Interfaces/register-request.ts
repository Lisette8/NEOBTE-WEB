export interface RegisterRequest {
  email: string;
  username: string;
  motDePasse: string;
  nom: string;
  prenom: string;
  cin: string;
  telephone: string;
  dateNaissance: string;
  job: string;
  genre?: 'HOMME' | 'FEMME';
  adresse?: string;
  codePostal?: string;
  pays?: string;
}
