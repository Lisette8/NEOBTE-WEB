import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../Services/auth-service';
import { LoginRequest } from '../../../Entities/Interfaces/login-request';
import { RegisterRequest } from '../../../Entities/Interfaces/register-request';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth-view',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './auth-view.html',
  styleUrl: './auth-view.css',
})
export class AuthView {
  isLoginMode: boolean = true;
  authForm: FormGroup;

  message = '';
  error = '';


  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router   
  ) {
    this.authForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', Validators.required],
      nom: [''],
      prenom: [''],
      age: [null],
      job: [''],
      genre: [''],
      adresse: ['']
    });
  }

  private updateValidatorsForMode() {
    const nom = this.authForm.get('nom');
    const prenom = this.authForm.get('prenom');
    const genre = this.authForm.get('genre');
    const adresse = this.authForm.get('adresse');

    if (this.isLoginMode) {
      nom?.clearValidators();
      prenom?.clearValidators();
      genre?.clearValidators();
      adresse?.clearValidators();
    } else {
      nom?.setValidators([Validators.required]);
      prenom?.setValidators([Validators.required]);
      genre?.setValidators([Validators.required]);
      adresse?.setValidators([Validators.required]);
    }

    nom?.updateValueAndValidity();
    prenom?.updateValueAndValidity();
    genre?.updateValueAndValidity();
    adresse?.updateValueAndValidity();
  }

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.updateValidatorsForMode();
  }


  //login request
  onLogin() {
    this.error = '';
    this.message = '';

    if (!this.authForm.get('email')?.valid || !this.authForm.get('motDePasse')?.valid) {
      this.error = 'Email et mot de passe sont obligatoires';
      this.authForm.markAllAsTouched();
      return;
    }

    const value = this.authForm.value as any;

    const data: LoginRequest = {
      email: value.email,
      motDePasse: value.motDePasse
    };

    this.authService.login(data).subscribe({
      next: () => {

        const role = this.authService.getUserRole();

        //redirect based on role
        if (role === 'ADMIN') {
          this.router.navigate(['/admin-dashboard']);
        } else {
          this.router.navigate(['/home-view']);
        }

      },
      error: () => {
        this.error = "Invalid credentials";
      }
    });
  }



  //register request
  onRegister() {
    this.error = '';
    this.message = '';

    this.updateValidatorsForMode();

    if (this.authForm.invalid) {
      this.error = 'Veuillez remplir tous les champs requis';
      this.authForm.markAllAsTouched();
      return;
    }

    const value = this.authForm.value as any;

    const data: RegisterRequest = {
      email: value.email,
      nom: value.nom,
      prenom: value.prenom,
      age: value.age,
      job: value.job,
      genre: value.genre as 'HOMME' | 'FEMME',
      adresse: value.adresse,
      motDePasse: value.motDePasse
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
