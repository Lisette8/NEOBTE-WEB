import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Compte } from '../Entities/Interfaces/compte';
import { DemandeCompte } from '../Entities/Interfaces/demande-compte';
import { DemandeCompteCreateDTO } from '../Entities/DTO/demande-compte-create-dto';

@Injectable({ providedIn: 'root' })
export class CompteService {
  private apiClient  = 'http://localhost:8080/api/v1/client/comptes';
  private apiAdmin   = 'http://localhost:8080/api/v1/admin/comptes';
  private apiDemande = 'http://localhost:8080/api/v1/client/demandes-compte';
  private apiAdminDemande = 'http://localhost:8080/api/v1/admin/demandes-compte';
 
  constructor(private http: HttpClient) {}
 
  // client
  getUserAccounts(userId: number): Observable<Compte[]> {
    return this.http.get<Compte[]>(`${this.apiClient}/utilisateur/${userId}`);
  }
 
  submitDemandeCompte(dto: DemandeCompteCreateDTO): Observable<DemandeCompte> {
    return this.http.post<DemandeCompte>(this.apiDemande, dto);
  }
 
  getMyDemandes(): Observable<DemandeCompte[]> {
    return this.http.get<DemandeCompte[]>(`${this.apiDemande}/mes-demandes`);
  }
 
  // ── Admin — Accounts ──
  getAllAccounts(): Observable<Compte[]> {
    return this.http.get<Compte[]>(`${this.apiAdmin}/all`);
  }
 
  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiAdmin}/${id}`);
  }
 
  // ── Admin — Demandes ──
  getAllDemandes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiAdminDemande}/all`);
  }
 
  getDemandesByStatut(statut: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiAdminDemande}/statut/${statut}`);
  }
 
  approveDemande(id: number, commentaire: string): Observable<any> {
    return this.http.put(`${this.apiAdminDemande}/${id}/approve`, { commentaireAdmin: commentaire });
  }
 
  rejectDemande(id: number, commentaire: string): Observable<any> {
    return this.http.put(`${this.apiAdminDemande}/${id}/reject`, { commentaireAdmin: commentaire });
  }
}
 