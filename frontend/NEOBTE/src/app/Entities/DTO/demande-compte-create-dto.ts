export interface DemandeCompteCreateDTO {
    typeCompte: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL';
    motif?: string;
    cin: string;
    dateNaissance: string;
    adresse?: string;
    job?: string;
    nomEntreprise?: string;
}