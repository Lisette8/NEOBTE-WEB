export interface RegisterRequest {
    email: string;
    nom: string;
    prenom: string;
    age: number | null;
    job: string;
    genre: 'HOMME' | 'FEMME';
    adresse: string;
    motDePasse: string;
}
