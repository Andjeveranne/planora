import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Récupérer le token depuis le localStorage
  const token = localStorage.getItem('token');
  
  // Si un token existe, l'ajouter aux headers
  if (token) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(authReq);
  }
  
  // Sinon, continuer avec la requête originale
  return next(req);
};