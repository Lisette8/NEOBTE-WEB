import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';
import { Virement } from '../Entities/Interfaces/virement';

export interface VirementHistoryFilter {
  search?: string;
  period?: 'today' | '7d' | '30d' | '3m' | 'all';
  type?: 'all' | 'sent' | 'received' | 'internal';
  sort?: 'date-desc' | 'date-asc' | 'amount-desc' | 'amount-asc';
  page?: number;
  size?: number;
}

export interface VirementHistoryPage {
  content: Virement[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  totalSent: number;
  totalReceived: number;
}
import { VirementCreateDTO } from '../Entities/DTO/virement-create-dto';
import { RecipientPreview } from '../Entities/Interfaces/recipient-preview';
import { InternalTransferCreateDTO } from '../Entities/DTO/internal-transfer-create-dto';
import { TransferConstraints } from '../Entities/Interfaces/transfer-constraints';

@Injectable({
  providedIn: 'root'
})
export class VirementService {

  private apiClient = 'http://localhost:8080/api/v1/client/virements';
  private apiAdmin = 'http://localhost:8080/api/v1/admin/virements';

  constructor(private http: HttpClient) { }

  // Resolve recipient before confirming — returns preview card
  resolveRecipient(identifier: string): Observable<RecipientPreview> {
    return this.http.get<RecipientPreview>(
      `${this.apiClient}/resolve-recipient?identifier=${encodeURIComponent(identifier)}`
    );
  }

  getConstraints(internal = false): Observable<TransferConstraints> {
    return this.http.get<TransferConstraints>(`${this.apiClient}/constraints?internal=${internal}`);
  }

  transfer(data: VirementCreateDTO): Observable<Virement> {
    return this.http.post<Virement>(`${this.apiClient}/transfer`, data);
  }

  transferInterne(data: InternalTransferCreateDTO): Observable<Virement> {
    return this.http.post<Virement>(`${this.apiClient}/transfer-interne`, data);
  }

  // History for the logged-in user (all their accounts) — legacy, kept for compat
  getHistory(): Observable<Virement[]> {
    return this.http.get<Virement[]>(`${this.apiClient}/history`);
  }

  // Filtered + paginated history — use this instead of getHistory() on all new clients
  getFilteredHistory(filter: VirementHistoryFilter): Observable<VirementHistoryPage> {
    let params = new HttpParams();
    if (filter.search) params = params.set('search', filter.search);
    if (filter.period) params = params.set('period', filter.period);
    if (filter.type) params = params.set('type', filter.type);
    if (filter.sort) params = params.set('sort', filter.sort);
    if (filter.page !== undefined) params = params.set('page', String(filter.page));
    if (filter.size !== undefined) params = params.set('size', String(filter.size));
    return this.http.get<VirementHistoryPage>(`${this.apiClient}/history/filter`, { params });
  }

  // Admin
  getAllVirements(): Observable<Virement[]> {
    return this.http.get<Virement[]>(`${this.apiAdmin}/all`);
  }
}
