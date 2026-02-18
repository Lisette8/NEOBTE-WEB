import { Routes } from '@angular/router';
import { HomeView } from './Components/Views/home-view/home-view';
import { AuthView } from './Components/Views/auth-view/auth-view';
import { Footer } from './Components/footer/footer';
import { Header } from './Components/header/header';

export const routes: Routes = [
    {
        path: '',
        component: HomeView,
    },
    {
        path: 'auth-view',
        component: AuthView,
    },
    {
        path: 'footer',
        component: Footer,
    },
    {
        path: 'header',
        component: Header,
    },

];
