import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Enseignant {
  idUser?: string;
  email: string;
  nomUser: string;
  prenomUser: string;
  telephone: string;
  role: string;
  specialite?: string;
  departement?: string;
}

export interface EnseignantRequest {
  email: string;
  mdp: string;
  nomUser: string;
  prenomUser: string;
  telephone: string;
  role: string;
  specialite: string;
  departement: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // Acces direct au UserService (port 8086)
  private apiUrl = 'http://localhost:8086/api/utilisateurs';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getEnseignants(): Observable<Enseignant[]> {
    return this.http.get<Enseignant[]>(`${this.apiUrl}/enseignants`, {
      headers: this.getHeaders()
    });
  }

  creerEnseignant(enseignant: EnseignantRequest): Observable<Enseignant> {
    return this.http.post<Enseignant>(`${this.apiUrl}/enseignant`, enseignant, {
      headers: this.getHeaders()
    });
  }

  updateEnseignant(id: string, enseignant: Partial<EnseignantRequest>): Observable<Enseignant> {
    return this.http.put<Enseignant>(`${this.apiUrl}/${id}`, enseignant, {
      headers: this.getHeaders()
    });
  }

  deleteEnseignant(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }
}
