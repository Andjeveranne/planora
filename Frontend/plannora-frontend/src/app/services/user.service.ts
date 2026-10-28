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
  idEcole?: string;
  idEcoles?: string[];
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
  idEcole: string;
  idEcoles: string[];
}

export interface DisponibiliteRequest {
  jourSemaine: string;
  heureDebut: string;
  heureFin: string;
  enseignantId: string;
}

export interface Disponibilite {
  id: number;
  jourSemaine: string;
  heureDebut: string;
  heureFin: string;
  enseignantId: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8081/api/utilisateurs';
  private apiDispoUrl = 'http://localhost:8081/api/disponibilites';

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

  rechercherEnseignants(idEcole?: string, departement?: string, specialite?: string): Observable<Enseignant[]> {
    let params: any = {};
    if (idEcole) params.idEcole = idEcole;
    if (departement) params.departement = departement;
    if (specialite) params.specialite = specialite;
    return this.http.get<Enseignant[]>(`${this.apiUrl}/enseignants/recherche`, {
      headers: this.getHeaders(),
      params
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

  // Méthodes pour les disponibilités

  getDisponibilitesEnseignant(enseignantId: string): Observable<Disponibilite[]> {
    return this.http.get<Disponibilite[]>(`${this.apiDispoUrl}/enseignant/${enseignantId}`, {
      headers: this.getHeaders()
    });
  }

  ajouterDisponibilite(dispo: DisponibiliteRequest): Observable<Disponibilite> {
    return this.http.post<Disponibilite>(this.apiDispoUrl, dispo, {
      headers: this.getHeaders()
    });
  }

  supprimerDisponibilite(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiDispoUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  // Méthode pour récupérer l'utilisateur connecté
  getCurrentUser(): any {
    try {
      const userStr = localStorage.getItem('currentUser');
      return userStr ? JSON.parse(userStr) : null;
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'utilisateur connecté:', error);
      return null;
    }
  }

  // Méthode pour vérifier si l'utilisateur est connecté
  isLoggedIn(): boolean {
    const token = localStorage.getItem('token');
    const user = this.getCurrentUser();
    return !!(token && user);
  }

  // Méthode pour vérifier le rôle de l'utilisateur
  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user && user.role === role;
  }
}
