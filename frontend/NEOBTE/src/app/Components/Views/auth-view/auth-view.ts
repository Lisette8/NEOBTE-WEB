import { CommonModule, NgClass } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../Services/auth-service';
import { LoginRequest } from '../../../Entities/Interfaces/login-request';
import { RegisterRequest } from '../../../Entities/Interfaces/register-request';

@Component({
  selector: 'app-auth-view',
  imports: [FormsModule, CommonModule],
  templateUrl: './auth-view.html',
  styleUrl: './auth-view.css',
})
export class AuthView {
  // toggle login/register
  isLoginMode: boolean = true;

  // form fields
  email = '';
  motDePasse = '';

  nom = '';
  prenom = '';
  age: number | null = null;
  job = '';
  genre: 'HOMME' | 'FEMME' | '' = '';
  adresse = '';

  message = '';
  error = '';

  constructor(private authService: AuthService) { }

  // 🔐 LOGIN
  onLogin() {
    this.error = '';
    this.message = '';

    const data: LoginRequest = {
      email: this.email,
      motDePasse: this.motDePasse
    };

    this.authService.login(data).subscribe({
      next: () => {
        this.message = "Login successfully";
      },
      error: () => {
        this.error = "Invalid credentials";
      }
    });
  }

  // 🆕 REGISTER
  onRegister() {
    this.error = '';
    this.message = '';

    if (!this.email || !this.motDePasse || !this.nom || !this.prenom || !this.genre || !this.adresse) {
      this.error = "Veuillez remplir tous les champs requis";
      return;
    }

    const data: RegisterRequest = {
      email: this.email,
      nom: this.nom,
      prenom: this.prenom,
      age: this.age,
      job: this.job,
      genre: this.genre as 'HOMME' | 'FEMME',
      adresse: this.adresse,
      motDePasse: this.motDePasse
    };

    this.authService.register(data).subscribe({
      next: () => {
        this.message = "Account created successfully";
        this.isLoginMode = true;
      },
      error: () => {
        this.error = "Registration failed";
      }
    });
  }
}
