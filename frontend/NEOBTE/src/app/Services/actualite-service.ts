import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Actualite } from '../Entities/Interfaces/actualite';
import { ActualiteCreateDTO } from '../Entities/DTO/actualite-create-dto';
import { Page } from '../Entities/Interfaces/page';



@Injectable({
  providedIn: 'root',
})
export class ActualiteService {

  private apiAdmin = 'http://localhost:8080/api/v1/admin/actualite';
  private apiClient = 'http://localhost:8080/api/v1/client/actualite';

  constructor(private http: HttpClient) { }


  //client side methods
  getAll(page: number, size: number) {
    return this.http.get<Page<Actualite>>(`${this.apiClient}/all?page=${page}&size=${size}`);
  }

  getById(id: number) {
    return this.http.get<Actualite>(`${this.apiClient}/${id}`);
  }



  //admin side methods
  create(data: ActualiteCreateDTO) {

    const token = localStorage.getItem('token');

    return this.http.post<Actualite>(`${this.apiAdmin}/add`, data, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  update(id: number, data: ActualiteCreateDTO) {
    const token = localStorage.getItem('token');
    
    return this.http.put<Actualite>(`${this.apiAdmin}/update/${id}`, data, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  delete(id: number) {

    const token = localStorage.getItem('token');

    return this.http.delete(`${this.apiAdmin}/delete/${id}` , {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
  }
}
