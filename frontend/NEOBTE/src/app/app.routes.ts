import { Routes } from '@angular/router';
import { HomeView } from './Views/home-view/home-view';
import { AuthView } from './Views/auth-view/auth-view';
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
