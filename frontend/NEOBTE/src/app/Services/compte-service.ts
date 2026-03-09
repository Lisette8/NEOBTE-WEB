import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Compte } from '../Entities/Interfaces/compte';

@Injectable({
  providedIn: 'root',
})
export class CompteService {
  private apiClient = 'http://localhost:8080/api/v1/client/comptes';
  private apiAdmin = 'http://localhost:8080/api/v1/admin/comptes';  

  constructor(private http: HttpClient) {}


  //admin methods
  getAllAccounts(): Observable<Compte[]>{
    return this.http.get<Compte[]>(`${this.apiAdmin}/all`);
  }

  getAccountById(id: number): Observable<Compte>{
    return this.http.get<Compte>(`${this.apiAdmin}/${id}`);
  }

  createAccount(account: Compte): Observable<Compte>{
    return this.http.post<Compte>(this.apiAdmin, account);
  }

  deleteAccount(id: number): Observable<Compte>{
    return this.http.delete<Compte>(`${this.apiAdmin}/${id}`);
  }



  //client methods
  getUserAccounts(userId: number): Observable<Compte[]>{
    return this.http.get<Compte[]>(`${this.apiClient}/utilisateur/${userId}`);
  }
}
