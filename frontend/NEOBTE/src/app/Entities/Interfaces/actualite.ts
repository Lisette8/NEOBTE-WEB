export interface Actualite {
  idActualite: number;
  titre: string;
  sousTitre?: string | null;
  description: string;
  contenu?: string | null;
  categorie?: string | null;
  imageUrl?: string | null;
  dateCreationActualite: string;
  dateMajActualite?: string;
  reactions?: Record<string, number>;
  myReaction?: string | null;
}
