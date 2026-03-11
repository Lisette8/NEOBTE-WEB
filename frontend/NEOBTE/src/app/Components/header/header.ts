import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../Services/auth-service';
import { TranslationService, Lang } from '../../Services/SharedServices/translation-service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  isMenuOpen = false;
  isLangOpen = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    public transService: TranslationService
  ) {}
  
  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  toggleLang() {
    this.isLangOpen = !this.isLangOpen;
  }

  setLang(lang: Lang) {
    this.transService.setLanguage(lang);
    this.isLangOpen = false;
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth-view']);
      },
      error: () => {
        // In case backend fails, the token will be removed...
        localStorage.removeItem('token');
        this.router.navigate(['/auth-view']);
      }
    });
  }
}
