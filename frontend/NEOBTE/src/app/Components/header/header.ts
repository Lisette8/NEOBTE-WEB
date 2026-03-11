import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
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
}
