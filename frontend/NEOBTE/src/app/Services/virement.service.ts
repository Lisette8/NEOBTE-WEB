import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Virement } from '../Entities/Interfaces/virement';
import { VirementCreateDTO } from '../Entities/DTO/virement-create-dto';

@Injectable({
  providedIn: 'root'
})
export class VirementService {

  private apiUrl = 'http://localhost:8080/api/client/virement';

  constructor(private http: HttpClient) {}

  transfer(data: VirementCreateDTO): Observable<Virement> {
    return this.http.post<Virement>(`${this.apiUrl}/transfer`, data);
  }

  getHistory(compteId: number): Observable<Virement[]> {
    return this.http.get<Virement[]>(`${this.apiUrl}/history/${compteId}`);
  }

}