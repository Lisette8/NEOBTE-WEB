import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../Services/auth-service';


export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/auth-view']);
    return false;
  }

  if (authService.getUserRole() === 'ADMIN') {
    router.navigate(['/admin-dashboard']);
    return false;
  }

  return true;
};
