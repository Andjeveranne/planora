import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export const debugInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('🔍 HTTP Request Debug:', {
    url: req.url,
    method: req.method,
    headers: req.headers.keys().reduce((acc, key) => {
      acc[key] = req.headers.get(key);
      return acc;
    }, {} as any),
    body: req.body
  });

  return next(req).pipe(
    tap({
      next: (event) => {
        if (event.type === 0) { // HttpEventType.Response
          console.log('✅ HTTP Response:', event);
        }
      },
      error: (error) => {
        console.error('❌ HTTP Error:', {
          status: error.status,
          statusText: error.statusText,
          url: error.url,
          message: error.message,
          error: error.error
        });
      }
    })
  );
};