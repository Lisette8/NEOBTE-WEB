import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../Entities/Classes/user';

@Injectable({
  providedIn: 'root',
})
export class AdminService {

  private api = "http://localhost:8080/api/admin";

  constructor(private http: HttpClient) {}



  //endpoints
  
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.api}/all`);
  }

  deleteUser(id: number) {
    return this.http.delete(`${this.api}/users/${id}`);
  }

}
