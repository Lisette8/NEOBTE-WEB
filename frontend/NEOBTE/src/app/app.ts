import { Component, signal, Renderer2, Inject } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { Header } from './Components/header/header';
import { Footer } from './Components/footer/footer';
import { CommonModule, DOCUMENT } from '@angular/common';
import { filter } from 'rxjs';
import { TranslationService, Lang } from './Services/translation-service';
import { ConfirmModal } from './Components/Shared/confirm-modal/confirm-modal';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Header, Footer, CommonModule, ConfirmModal],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('NEOBTE');
  showLayout = true;

  constructor(
    private router: Router, 
    private transService: TranslationService,
    private renderer: Renderer2,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.urlAfterRedirects || event.url;
      // Show header/footer for auth, home, and landing pages
      this.showLayout = url.includes('auth-view') || url.includes('home-view') || url.includes('landing-view') || url === '/';
    });

    // Handle Language & RTL
    this.transService.lang$.subscribe((lang: Lang) => {
      this.renderer.setAttribute(this.document.documentElement, 'lang', lang);
      const dir = lang === 'ar' ? 'rtl' : 'ltr';
      this.renderer.setAttribute(this.document.documentElement, 'dir', dir);
    });

    // Load saved lang
    const savedLang = localStorage.getItem('lang') as Lang;
    if (savedLang) this.transService.setLanguage(savedLang);
  }
}
