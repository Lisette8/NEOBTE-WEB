export interface DemandeCompte{
    idDemande: number;
    typeCompte: string;
    motif?: string;
    statutDemande: 'EN_ATTENTE' | 'APPROUVEE' | 'REJETEE';
    dateDemande: string;
    dateDecision?: string;
    commentaireAdmin?: string;
    compteOuvertId?: number;
}