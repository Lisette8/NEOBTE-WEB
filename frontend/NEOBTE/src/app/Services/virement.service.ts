import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class VirementService {

  private apiUrl = 'http://localhost:8080/api/client/virement';

  constructor(private http: HttpClient) {}

  transfer(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/transfer`, data);
  }

  getHistory(compteId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/history/${compteId}`);
  }
}