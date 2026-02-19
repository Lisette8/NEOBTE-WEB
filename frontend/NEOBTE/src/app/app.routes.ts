import { Routes } from '@angular/router';
import { HomeView } from './Components/Views/home-view/home-view';
import { AuthView } from './Components/Views/auth-view/auth-view';
import { Footer } from './Components/footer/footer';
import { Header } from './Components/header/header';
import { AdminDashboard } from './Components/Views/admin-dashboard/admin-dashboard';
import { UserManagement } from './Components/Views/user-management/user-management';
import { LandingView } from './Components/Views/landing-view/landing-view';

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
    {
        path: 'admin-dashboard',
        component: AdminDashboard,
    },
    {
        path: 'user-management',
        component: UserManagement,
    },
    {
        path: 'landingView',
        component: LandingView,
    },

];
