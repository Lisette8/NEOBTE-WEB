import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Actualite } from '../Entities/Interfaces/actualite';
import { ActualiteCreateDTO } from '../Entities/DTO/actualite-create-dto';



@Injectable({
  providedIn: 'root',
})
export class ActualiteService {

  private apiAdmin = 'http://localhost:8080/api/admin/actualite';
  private apiClient = 'http://localhost:8080/api/client/actualite';

  constructor(private http: HttpClient) {}


  //client side methods
  getAll() {
    return this.http.get<Actualite[]>(this.apiClient);
  }



  //admin side methods
create(data: ActualiteCreateDTO) {
    return this.http.post<Actualite>(this.apiAdmin, data);
  }

  update(id: number, data: ActualiteCreateDTO) {
    return this.http.put<Actualite>(`${this.apiAdmin}/${id}`, data);
  }

  delete(id: number) {
    return this.http.delete(`${this.apiAdmin}/${id}`);
  }
}
