export interface DemandeCompteCreateDTO {
    typeCompte: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL';
    motif?: string;
    cin?: string;           // omitted when already on the user's profile
    dateNaissance?: string; // omitted when already on the user's profile
    adresse?: string;
    job?: string;
    nomEntreprise?: string;
}
