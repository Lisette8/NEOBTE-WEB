import { Utilisateur } from "./utilisateur";

export class Client extends Utilisateur {
    nom!: string;
    prenom!: string;
    adresse!: string;
    age!: number;
    job!: string;
    genre!: string;
    solde!: number;
}
