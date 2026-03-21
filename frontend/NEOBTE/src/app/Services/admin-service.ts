import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../Entities/Classes/user';
import { UserListDTO } from '../Entities/DTO/Admin/user-list-dto';
import { UserUpdateDTO } from '../Entities/DTO/Admin/user-update-dto';
import { UserCreateDTO } from '../Entities/DTO/Admin/user-create-dto';

export interface AdminStats {
  totalClients: number;
  totalAdmins: number;
  totalTransfers: number;
  totalVolume: number;
  avgTransfer: number;
  totalAccounts: number;
}

export interface GlobalSearchResult {
  users: SearchUser[];
  accounts: SearchAccount[];
  transfers: SearchTransfer[];
  tickets: SearchTicket[];
}

export interface SearchUser { id: number; fullName: string; email: string; role: string; premium: boolean; }
export interface SearchAccount { id: number; type: string; statut: string; solde: number; userId: number; userFullName: string; }
export interface SearchTransfer { id: number; montant: number; senderName: string; recipientName: string; date: string; }
export interface SearchTicket { id: number; sujet: string; status: string; userEmail: string; }

@Injectable({ providedIn: 'root' })
export class AdminService {

  private api = 'http://localhost:8080/api/v1/admin';

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<UserListDTO[]> {
    return this.http.get<UserListDTO[]>(`${this.api}/all`);
  }

  getUserById(id: number) {
    return this.http.get<User>(`${this.api}/users/${id}`);
  }

  createUser(user: UserCreateDTO) {
    return this.http.post(`${this.api}/users`, user);
  }

  updateUser(id: number, user: UserUpdateDTO): Observable<any> {
    return this.http.put(`${this.api}/users/${id}`, user);
  }

  deleteUser(id: number) {
    return this.http.delete(`${this.api}/users/${id}`);
  }

  setPremium(userId: number, premium: boolean): Observable<{ userId: number; premium: boolean }> {
    return this.http.put<{ userId: number; premium: boolean }>(
      `${this.api}/users/${userId}/premium`,
      { premium }
    );
  }

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.api}/stats`);
  }

  globalSearch(query: string): Observable<GlobalSearchResult> {
    return this.http.get<GlobalSearchResult>(`${this.api}/search`, { params: { q: query } });
  }
}