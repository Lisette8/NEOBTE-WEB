export interface UserCreateDTO {
  email: string;
  nom: string;
  prenom: string;
  age?: number;
  adresse?: string;
  job?: string;
  genre?: string;
}