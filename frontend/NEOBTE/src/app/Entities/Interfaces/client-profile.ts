export interface ClientProfile {
  id: number;
  email: string;
  username: string;
  nom: string;
  prenom: string;
  photoUrl?: string | null;
  cin?: string | null;
  telephone?: string | null;
  adresse?: string | null;
  codePostal?: string | null;
  pays?: string | null;
  dateNaissance?: string | null;
  job?: string | null;
  genre?: 'HOMME' | 'FEMME' | null;
  role?: 'ADMIN' | 'CLIENT';
  dateCreationCompte?: string | null;
}

export interface UpdateClientProfileRequest {
  nom: string;
  prenom: string;
  telephone: string;
  job: string;
  genre?: 'HOMME' | 'FEMME' | null;
  adresse?: string | null;
  codePostal?: string | null;
  pays?: string | null;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

