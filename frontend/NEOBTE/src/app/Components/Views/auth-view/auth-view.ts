import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
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


export class AuthView implements OnInit {
  isLoginMode: boolean = true;
  authForm: FormGroup;
  showPassword = false;
  message = '';
  error = '';

  // Min date: 18 years ago, max date: today
  readonly maxDate = new Date().toISOString().split('T')[0];
  readonly minDate = (() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 100);
    return d.toISOString().split('T')[0];
  })();

  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.authForm = this.fb.group({
      // Login fields
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', Validators.required],

      // Register fields
      username: [''],
      nom: [''],
      prenom: [''],
      cin: [''],
      telephone: [''],
      dateNaissance: [''],
      job: [''],
      genre: [''],
      adresse: [''],
      codePostal: [''],
      pays: ['Tunisie'],
    });
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getUserRole();
      this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
    }
  }

  private updateValidatorsForMode() {
    const required = (ctrl: AbstractControl | null) => {
      ctrl?.setValidators([Validators.required]);
      ctrl?.updateValueAndValidity();
    };
    const optional = (ctrl: AbstractControl | null) => {
      ctrl?.clearValidators();
      ctrl?.updateValueAndValidity();
    };

    if (this.isLoginMode) {
      ['username','nom','prenom','cin','telephone','dateNaissance','job','genre','adresse','codePostal','pays']
        .forEach(f => optional(this.authForm.get(f)));
      this.authForm.get('motDePasse')?.setValidators([Validators.required]);
    } else {
      required(this.authForm.get('username'));
      required(this.authForm.get('nom'));
      required(this.authForm.get('prenom'));
      this.authForm.get('cin')?.setValidators([
        Validators.required,
        Validators.pattern(/^[0-9]{8}$/)
      ]);
      this.authForm.get('telephone')?.setValidators([
        Validators.required,
        Validators.pattern(/^[+]?[0-9]{8,15}$/)
      ]);
      this.authForm.get('dateNaissance')?.setValidators([
        Validators.required,
        this.ageValidator(18)
      ]);
      required(this.authForm.get('job'));
      this.authForm.get('motDePasse')?.setValidators([
        Validators.required,
        Validators.minLength(8)
      ]);
      ['cin','telephone','dateNaissance','genre','adresse','codePostal','pays']
        .forEach(f => this.authForm.get(f)?.updateValueAndValidity());
    }
  }

  // Custom validator: user must be at least `minAge` years old
  private ageValidator(minAge: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const dob = new Date(control.value);
      const today = new Date();
      const age = today.getFullYear() - dob.getFullYear() -
        (today < new Date(today.getFullYear(), dob.getMonth(), dob.getDate()) ? 1 : 0);
      return age >= minAge ? null : { underage: { required: minAge, actual: age } };
    };
  }

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.error = '';
    this.message = '';
    this.authForm.reset({ pays: 'Tunisie' });
    this.updateValidatorsForMode();
  }

  fieldInvalid(field: string): boolean {
    const ctrl = this.authForm.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }

  onLogin() {
    this.error = '';
    this.message = '';

    if (!this.authForm.get('email')?.valid || !this.authForm.get('motDePasse')?.valid) {
      this.error = 'Email and password are required';
      this.authForm.markAllAsTouched();
      return;
    }

    const data: LoginRequest = {
      email: this.authForm.value.email,
      motDePasse: this.authForm.value.motDePasse
    };

    this.authService.login(data).subscribe({
      next: () => {
        const role = this.authService.getUserRole();
        this.router.navigate([role === 'ADMIN' ? '/admin-dashboard' : '/home-view']);
      },
      error: () => {
        this.error = 'Invalid credentials. Please try again.';
      }
    });
  }

  onRegister() {
    this.error = '';
    this.message = '';
    this.updateValidatorsForMode();

    if (this.authForm.invalid) {
      this.authForm.markAllAsTouched();
      this.error = 'Please fill in all required fields correctly.';
      return;
    }

    const v = this.authForm.value;

    const data: RegisterRequest = {
      email: v.email,
      username: v.username,
      motDePasse: v.motDePasse,
      nom: v.nom,
      prenom: v.prenom,
      cin: v.cin,
      telephone: v.telephone,
      dateNaissance: v.dateNaissance,
      job: v.job,
      genre: v.genre || undefined,
      adresse: v.adresse || undefined,
      codePostal: v.codePostal || undefined,
      pays: v.pays || 'Tunisie',
    };

    this.authService.register(data).subscribe({
      next: () => {
        this.message = 'Account created successfully! You can now log in.';
        this.isLoginMode = true;
        this.authForm.reset({ pays: 'Tunisie' });
      },
      error: (err) => {
        this.error = err?.error?.message || 'Registration failed. Please try again.';
      }
    });
  }
}
