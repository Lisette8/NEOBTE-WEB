import { SupportCategorie, SupportPriorite } from '../Interfaces/support';

export interface SupportCreateDTO {
  sujet: string;
  message: string;
  categorie: SupportCategorie;
  priorite: SupportPriorite;
}
