import { CommonModule, NgClass } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../Services/auth-service';

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

  message = '';
  error = '';

  constructor(private authService: AuthService) {}

  // 🔐 LOGIN
  onLogin() {
    this.error = '';
    this.message = '';

    this.authService.login(this.email, this.motDePasse).subscribe({
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

    const data = {
      email: this.email,
      nom: this.nom,
      prenom: this.prenom,
      age: this.age,
      job: this.job,
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
