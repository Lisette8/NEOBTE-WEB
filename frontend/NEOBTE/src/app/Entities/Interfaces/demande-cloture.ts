export interface DemandeCloture {
  id: number;
  compteId: number;
  typeCompte: string;
  utilisateurId: number;
  utilisateurNom: string;
  utilisateurPrenom: string;
  utilisateurEmail: string;
  motif: string;
  statut: 'EN_ATTENTE' | 'APPROUVEE' | 'REJETEE';
  commentaireAdmin?: string;
  dateDemande: string;
  dateDecision?: string;
  soldeAtDemande: number;
}
 