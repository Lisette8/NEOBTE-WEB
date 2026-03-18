import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Compte } from '../Entities/Interfaces/compte';
import { DemandeCompte } from '../Entities/Interfaces/demande-compte';
import { DemandeCompteCreateDTO } from '../Entities/DTO/demande-compte-create-dto';
import { DemandeCloture } from '../Entities/Interfaces/demande-cloture';


@Injectable({ providedIn: 'root' })
export class CompteService {
  private apiClient       = 'http://localhost:8080/api/v1/client/comptes';
  private apiDemande      = 'http://localhost:8080/api/v1/client/demandes-compte';
  private apiAdmin        = 'http://localhost:8080/api/v1/admin/comptes';
  private apiAdminDemande = 'http://localhost:8080/api/v1/admin/demandes-compte';
 
  constructor(private http: HttpClient) {}
 
  // ── Client — accounts ──
  getMyAccounts(): Observable<Compte[]> {
    return this.http.get<Compte[]>(`${this.apiClient}/me`);
  }

  getUserAccounts(userId: number): Observable<Compte[]> {
    return this.http.get<Compte[]>(`${this.apiClient}/utilisateur/${userId}`);
  }
 
  suspendreCompte(compteId: number): Observable<Compte> {
    return this.http.put<Compte>(`${this.apiClient}/${compteId}/suspendre`, {});
  }
 
  reactiverCompte(compteId: number): Observable<Compte> {
    return this.http.put<Compte>(`${this.apiClient}/${compteId}/reactiver`, {});
  }
 
  demanderCloture(compteId: number, motif: string): Observable<DemandeCloture> {
    return this.http.post<DemandeCloture>(`${this.apiClient}/demande-cloture`, { compteId, motif });
  }
 
  annulerCloture(compteId: number): Observable<Compte> {
    return this.http.put<Compte>(`${this.apiClient}/${compteId}/annuler-cloture`, {});
  }
 
  getMesDemandesCloture(): Observable<DemandeCloture[]> {
    return this.http.get<DemandeCloture[]>(`${this.apiClient}/mes-demandes-cloture`);
  }
 
  // ── Client — account opening ──
  submitDemandeCompte(dto: DemandeCompteCreateDTO): Observable<DemandeCompte> {
    return this.http.post<DemandeCompte>(this.apiDemande, dto);
  }
 
  getMyDemandes(): Observable<DemandeCompte[]> {
    return this.http.get<DemandeCompte[]>(`${this.apiDemande}/mes-demandes`);
  }
 
  // ── Admin — accounts ──
  getAllAccounts(): Observable<Compte[]> {
    return this.http.get<Compte[]>(`${this.apiAdmin}/all`);
  }
 
  updateStatutCompte(compteId: number, newStatut: string, commentaire?: string): Observable<Compte> {
    return this.http.put<Compte>(`${this.apiAdmin}/${compteId}/statut`, { newStatut, commentaire });
  }
 
  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiAdmin}/${id}`);
  }
 



  //Admin
  getAllDemandesCloture(): Observable<DemandeCloture[]> {
    return this.http.get<DemandeCloture[]>(`${this.apiAdmin}/demandes-cloture`);
  }
 
  approuverCloture(id: number, commentaire?: string): Observable<DemandeCloture> {
    return this.http.put<DemandeCloture>(`${this.apiAdmin}/demandes-cloture/${id}/approuver`, { commentaire });
  }
 
  rejeterCloture(id: number, commentaire: string): Observable<DemandeCloture> {
    return this.http.put<DemandeCloture>(`${this.apiAdmin}/demandes-cloture/${id}/rejeter`, { commentaire });
  }
 
  // ── Admin — account opening demandes ──
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
