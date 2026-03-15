import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Virement } from '../Entities/Interfaces/virement';
import { VirementCreateDTO } from '../Entities/DTO/virement-create-dto';
import { RecipientPreview } from '../Entities/Interfaces/recipient-preview';

@Injectable({
  providedIn: 'root'
})
export class VirementService {
 
  private apiClient = 'http://localhost:8080/api/v1/client/virements';
  private apiAdmin = 'http://localhost:8080/api/v1/admin/virements';
 
  constructor(private http: HttpClient) {}
 
  // Resolve recipient before confirming — returns preview card
  resolveRecipient(identifier: string): Observable<RecipientPreview> {
    return this.http.get<RecipientPreview>(
      `${this.apiClient}/resolve-recipient?identifier=${encodeURIComponent(identifier)}`
    );
  }
 
  transfer(data: VirementCreateDTO): Observable<Virement> {
    return this.http.post<Virement>(`${this.apiClient}/transfer`, data);
  }
 
  // History for the logged-in user (all their accounts)
  getHistory(): Observable<Virement[]> {
    return this.http.get<Virement[]>(`${this.apiClient}/history`);
  }
 
  // Admin
  getAllVirements(): Observable<Virement[]> {
    return this.http.get<Virement[]>(`${this.apiAdmin}/all`);
  }
}