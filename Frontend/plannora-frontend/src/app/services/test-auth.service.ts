import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TestAuthService {
  private baseUrl = 'http://localhost:8081/api/salles';

  constructor(private http: HttpClient) {}

  // Test endpoint public
  testHealth(): Observable<any> {
    return this.http.get(`${this.baseUrl}/health`);
  }

  // Test endpoint avec authentification
  testAuth(): Observable<any> {
    return this.http.get(`${this.baseUrl}/debug/auth`);
  }

  // Test liste des salles (authentifié)
  testSalles(): Observable<any> {
    return this.http.get(`${this.baseUrl}`);
  }

  // Vérifier le token dans localStorage
  checkToken(): any {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('currentUser');
    
    return {
      hasToken: !!token,
      tokenLength: token ? token.length : 0,
      tokenPreview: token ? token.substring(0, 20) + '...' : null,
      user: user ? JSON.parse(user) : null
    };
  }
}