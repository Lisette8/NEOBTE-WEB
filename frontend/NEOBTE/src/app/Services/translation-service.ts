import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type Lang = 'fr' | 'en' | 'ar';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private currentLang = new BehaviorSubject<Lang>('fr');
  lang$ = this.currentLang.asObservable();

  private translations: Record<Lang, any> = {
    fr: {
      common: {
        connexion: 'Connexion',
        inscription: 'Inscription',
        accueil: 'Accueil',
        produits: 'Produits',
        support: 'Support',
        logout: 'Déconnexion',
        email: 'Email',
        password: 'Mot de passe',
        nom: 'Nom',
        prenom: 'Prénom',
        age: 'Âge',
        genre: 'Genre',
        profession: 'Profession',
        adresse: 'Adresse',
        homme: 'Homme',
        femme: 'Femme',
        select: 'Sélectionner'
      },
      auth: {
        welcome_back: 'Bon retour',
        join_us: 'Rejoignez-nous',
        subtitle_login: 'Accédez à votre compte NEO BTE',
        subtitle_register: 'Commencez votre voyage bancaire moderne',
        login_btn: 'Se connecter',
        register_btn: "S'inscrire",
        switch_register: "Pas encore de compte ? S'inscrire",
        switch_login: 'Déjà un compte ? Se connecter'
      },
      footer: {
        desc: 'Votre partenaire de confiance pour une banque moderne en Tunisie. Faisant partie du réseau de la Banque de Tunisie et des Émirats.',
        navigation: 'Navigation',
        legal: 'Légal',
        contact: 'Contact',
        rights: 'Tous droits réservés. Filiale de la Banque de Tunisie et des Émirats.'
      }
    },
    en: {
      common: {
        connexion: 'Login',
        inscription: 'Register',
        accueil: 'Home',
        produits: 'Products',
        support: 'Support',
        logout: 'Logout',
        email: 'Email',
        password: 'Password',
        nom: 'Last Name',
        prenom: 'First Name',
        age: 'Age',
        genre: 'Gender',
        profession: 'Profession',
        adresse: 'Address',
        homme: 'Male',
        femme: 'Female',
        select: 'Select'
      },
      auth: {
        welcome_back: 'Welcome Back',
        join_us: 'Join Us',
        subtitle_login: 'Access your NEO BTE account',
        subtitle_register: 'Start your modern banking journey',
        login_btn: 'Sign In',
        register_btn: 'Sign Up',
        switch_register: "Don't have an account? Sign Up",
        switch_login: 'Already have an account? Sign In'
      },
      footer: {
        desc: 'Your trusted partner for modern banking in Tunisia. Part of the Bank of Tunisia and Emirates network.',
        navigation: 'Navigation',
        legal: 'Legal',
        contact: 'Contact',
        rights: 'All rights reserved. Subsidiary of the Bank of Tunisia and Emirates.'
      }
    },
    ar: {
      common: {
        connexion: 'تسجيل الدخول',
        inscription: 'إنشاء حساب',
        accueil: 'الرئيسية',
        produits: 'المنتجات',
        support: 'الدعم',
        logout: 'تسجيل الخروج',
        email: 'البريد الإلكتروني',
        password: 'كلمة المرور',
        nom: 'اللقب',
        prenom: 'الاسم',
        age: 'العمر',
        genre: 'الجنس',
        profession: 'المهنة',
        adresse: 'العنوان',
        homme: 'ذكر',
        femme: 'أنثى',
        select: 'اختر'
      },
      auth: {
        welcome_back: 'مرحباً بعودتك',
        join_us: 'انضم إلينا',
        subtitle_login: 'الولوج إلى حسابك NEO BTE',
        subtitle_register: 'ابدأ رحلتك المصرفية الحديثة',
        login_btn: 'دخول',
        register_btn: 'تسجيل',
        switch_register: 'ليس لديك حساب؟ سجل الآن',
        switch_login: 'لديك حساب بالفعل؟ سجل الدخول'
      },
      footer: {
        desc: 'شريكك الموثوق لبنك حديث في تونس. جزء من شبكة بنك تونس والإمارات.',
        navigation: 'التنقل',
        legal: 'قانوني',
        contact: 'اتصال',
        rights: 'جميع الحقوق محفوظة. فرع من بنك تونس والإمارات.'
      }
    }
  };

  setLanguage(lang: Lang) {
    this.currentLang.next(lang);
    localStorage.setItem('lang', lang);
  }

  getLanguage(): Lang {
    return this.currentLang.value;
  }

  translate(key: string): string {
    const keys = key.split('.');
    let result = this.translations[this.currentLang.value];
    for (const k of keys) {
      if (result[k]) {
        result = result[k];
      } else {
        return key;
      }
    }
    return result;
  }
}
