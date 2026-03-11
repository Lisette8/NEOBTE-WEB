import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Virement } from '../Entities/Interfaces/virement';
import { VirementCreateDTO } from '../Entities/DTO/virement-create-dto';

@Injectable({
  providedIn: 'root'
})
export class VirementService {

  private apiClient = 'http://localhost:8080/api/v1/client/virements';
  private apiAdmin = 'http://localhost:8080/api/v1/admin/virements';

  constructor(private http: HttpClient) {}



  //admin methods
  getAllVirements(): Observable<Virement[]> {
    return this.http.get<Virement[]>(`${this.apiAdmin}/all`);
  }

  getVirementById(id: number): Observable<Virement> {
    return this.http.get<Virement>(`${this.apiAdmin}/${id}`);
  }



  //client methods
  transfer(data: VirementCreateDTO): Observable<Virement> {
    return this.http.post<Virement>(`${this.apiClient}/transfer`, data);
  }

  getHistory(compteId: number): Observable<Virement[]> {
    return this.http.get<Virement[]>(`${this.apiClient}/history/${compteId}`);
  }

}