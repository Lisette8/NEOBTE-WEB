import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../Services/auth-service';

export const loginGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    const role = authService.getUserRole();
    
    if (role === 'ADMIN') {
      router.navigate(['/admin-dashboard']);
    } else {
      router.navigate(['/home-view']);
    }
    
    return false;
  }

  return true;
};
