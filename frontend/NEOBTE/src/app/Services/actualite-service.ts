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

  // Client side methods
  getAll(page: number, size: number) {
    return this.http.get<Page<Actualite>>(`${this.apiClient}/all?page=${page}&size=${size}`);
  }

  getById(id: number) {
    return this.http.get<Actualite>(`${this.apiClient}/${id}`);
  }

  // Admin side methods — authInterceptor already injects the token, no need to add it manually
  create(data: ActualiteCreateDTO, image?: File | null) {
    if (image) {
      const fd = new FormData();
      fd.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
      fd.append('image', image);
      return this.http.post<Actualite>(`${this.apiAdmin}/add`, fd);
    }
    return this.http.post<Actualite>(`${this.apiAdmin}/add`, data);
  }

  update(id: number, data: ActualiteCreateDTO, image?: File | null) {
    if (image) {
      const fd = new FormData();
      fd.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
      fd.append('image', image);
      return this.http.put<Actualite>(`${this.apiAdmin}/update/${id}`, fd);
    }
    return this.http.put<Actualite>(`${this.apiAdmin}/update/${id}`, data);
  }

  delete(id: number) {
    return this.http.delete(`${this.apiAdmin}/delete/${id}`);
  }

  react(id: number, reaction: string) {
    return this.http.post<Actualite>(`${this.apiClient}/${id}/reaction`, { reaction });
  }
}
