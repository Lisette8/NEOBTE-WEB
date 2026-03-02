import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { throwError } from 'rxjs';

// Simplified client-side rate limiter
// In a real production app, this should be handled primarily on the backend
const requestCounts = new Map<string, { count: number, timestamp: number }>();
const LIMIT = 10; // Max requests
const WINDOW_MS = 10000; // per 10 seconds

export const rateLimiterInterceptor: HttpInterceptorFn = (req, next) => {
  // We only rate limit API calls
  if (!req.url.includes('/api/')) {
    return next(req);
  }

  const now = Date.now();
  const url = req.url;
  const method = req.method;
  const key = `${method}:${url}`;

  const requestInfo = requestCounts.get(key);

  if (!requestInfo) {
    requestCounts.set(key, { count: 1, timestamp: now });
  } 
  else {
    if (now - requestInfo.timestamp > WINDOW_MS) {
      // Reset window
      requestCounts.set(key, { count: 1, timestamp: now });
    }
    else {
      if (requestInfo.count >= LIMIT) {
        console.warn(`Rate limit exceeded for ${key}`);
        return throwError(() => new Error('Too many requests. Please slow down.'));
      }
      requestInfo.count++;
    }
  }

  return next(req);
};
