import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SupportCreateDTO } from '../Entities/DTO/support-create-dto';
import { Support } from '../Entities/Interfaces/support';

@Injectable({
  providedIn: 'root',
})

export class SupportService {

  private apiClient = 'http://localhost:8080/api/v1/client/support';
  private apiAdmin = 'http://localhost:8080/api/v1/admin/support';

  constructor(private http: HttpClient) { }


  // client side methods
  createTicket(data: SupportCreateDTO) {
    return this.http.post<Support>(`${this.apiClient}/add`, data);
  }

  getMyTickets() {
    return this.http.get<Support[]>(`${this.apiClient}/myTickets`);
  }


  // admin side methods
  getAllTickets() {
    return this.http.get<Support[]>(`${this.apiAdmin}/all`);
  }

  updateTicket(id: number, response: string, status: string) {
    return this.http.put<Support>(`${this.apiAdmin}/update/${id}?response=${response}&status=${status}`, {
      response,
      status
    });
  }

  deleteTicket(id: number) {
    return this.http.delete(`${this.apiAdmin}/delete/${id}`);
  }

  aiSuggest(sujet: string, message: string, senderName: string) {
    return this.http.post<{ suggestion: string }>(`${this.apiAdmin}/ai-suggest`, { sujet, message, senderName });
  }
}
