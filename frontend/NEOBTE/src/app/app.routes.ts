import { Routes } from '@angular/router';
import { HomeView } from './Components/Views/home-view/home-view';
import { AuthView } from './Components/Views/auth-view/auth-view';
import { Footer } from './Components/footer/footer';
import { Header } from './Components/header/header';
import { AdminDashboard } from './Components/Views/admin-dashboard/admin-dashboard';
import { UserManagement } from './Components/Views/user-management/user-management';
import { LandingView } from './Components/Views/landing-view/landing-view';
import { adminGuard } from './Security/Guards/AdminGuard';
import { authGuard } from './Security/Guards/AuthGuard';

export const routes: Routes = [
    {
        path: 'home-view',
        component: HomeView,
        canActivate: [authGuard],
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
        canActivate: [authGuard, adminGuard],
    },
    {
        path: 'user-management',
        component: UserManagement,
    },
    {
        path: 'landingView',
        component: LandingView,
    },
    {
        path: '',
        redirectTo: '/auth-view',
        pathMatch: 'full',
    },
    {
        path: '**',
        redirectTo: '/auth-view',
    },


];
