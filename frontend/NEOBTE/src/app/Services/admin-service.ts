import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../Entities/Classes/user';
import { UserListDTO } from '../Entities/DTO/Admin/user-list-dto';
import { UserUpdateDTO } from '../Entities/DTO/Admin/user-update-dto';
import { UserCreateDTO } from '../Entities/DTO/Admin/user-create-dto';

@Injectable({
  providedIn: 'root',
})
export class AdminService {

  private api = "http://localhost:8080/api/admin";

  constructor(private http: HttpClient) { }



  //endpoints

  getAllUsers(): Observable<UserListDTO[]> {
    return this.http.get<UserListDTO[]>(`${this.api}/all`);
  }

  getUserById(id: number) {
    return this.http.get<User>(`${this.api}/users/${id}`);
  }


  //admin crud

  createUser(user: UserCreateDTO) {
    return this.http.post(`${this.api}/users`, user);
  }

  updateUser(id: number, user: UserUpdateDTO): Observable<any> {
    return this.http.put(`${this.api}/users/${id}`, user);
  }

  deleteUser(id: number) {
    return this.http.delete(`${this.api}/users/${id}`);
  }

}
