import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SalleDTO {
  idSalle?: string;
  nomSalle: string;
  type: TypeSalle;
  localisation: string;
  capacite: number;
  capaciteExam: number;
  idEcole: string;
  idClasse?: string | null;
  statut: StatutSalle;
}

export enum TypeSalle {
  AMPHITHEATRE = 'AMPHITHEATRE',
  SALLE_COURS = 'SALLE_COURS',
  SALLE_COURS_AMPHITHEATRE = 'SALLE_COURS_AMPHITHEATRE',
  LABORATOIRE = 'LABORATOIRE',
  SALLE_INFORMATIQUE = 'SALLE_INFORMATIQUE',
  SALLE_CONFERENCE = 'SALLE_CONFERENCE',
  BIBLIOTHEQUE = 'BIBLIOTHEQUE',
  SALLE_REUNION = 'SALLE_REUNION'
}

export enum StatutSalle {
  DISPONIBLE = 'DISPONIBLE',
  OCCUPEE = 'OCCUPEE',
  MAINTENANCE = 'MAINTENANCE',
  HORS_SERVICE = 'HORS_SERVICE'
}

@Injectable({
  providedIn: 'root'
})
export class SalleService {
  private apiUrl = 'http://localhost:8081/api/salles'; // Gateway URL

  constructor(private http: HttpClient) {}

  // Créer une nouvelle salle
  creerSalle(salle: SalleDTO): Observable<SalleDTO> {
    return this.http.post<SalleDTO>(this.apiUrl, salle);
  }

  // Obtenir toutes les salles
  obtenirToutesLesSalles(): Observable<SalleDTO[]> {
    return this.http.get<SalleDTO[]>(this.apiUrl);
  }

  // Obtenir une salle par ID
  obtenirSalleParId(idSalle: string): Observable<SalleDTO> {
    return this.http.get<SalleDTO>(`${this.apiUrl}/${idSalle}`);
  }

  // Obtenir les salles par école
  obtenirSallesParEcole(idEcole: string): Observable<SalleDTO[]> {
    return this.http.get<SalleDTO[]>(`${this.apiUrl}/ecole/${idEcole}`);
  }

  // Obtenir les salles par type
  obtenirSallesParType(type: TypeSalle): Observable<SalleDTO[]> {
    return this.http.get<SalleDTO[]>(`${this.apiUrl}/type/${type}`);
  }

  // Obtenir les salles par statut
  obtenirSallesParStatut(statut: StatutSalle): Observable<SalleDTO[]> {
    return this.http.get<SalleDTO[]>(`${this.apiUrl}/statut/${statut}`);
  }

  // Obtenir les salles disponibles
  obtenirSallesDisponibles(idEcole: string, capaciteMin: number = 1): Observable<SalleDTO[]> {
    const params = new HttpParams()
      .set('idEcole', idEcole)
      .set('capaciteMin', capaciteMin.toString());
    return this.http.get<SalleDTO[]>(`${this.apiUrl}/disponibles`, { params });
  }

  // Mettre à jour une salle
  mettreAJourSalle(idSalle: string, salle: SalleDTO): Observable<SalleDTO> {
    return this.http.put<SalleDTO>(`${this.apiUrl}/${idSalle}`, salle);
  }

  // Changer le statut d'une salle
  changerStatutSalle(idSalle: string, statut: StatutSalle): Observable<SalleDTO> {
    const params = new HttpParams().set('statut', statut);
    return this.http.patch<SalleDTO>(`${this.apiUrl}/${idSalle}/statut`, null, { params });
  }

  // Supprimer une salle
  supprimerSalle(idSalle: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${idSalle}`);
  }

  // Obtenir les types de salles
  obtenirTypesSalles(): Observable<TypeSalle[]> {
    return this.http.get<TypeSalle[]>(`${this.apiUrl}/types`);
  }

  // Obtenir les statuts de salles
  obtenirStatutsSalles(): Observable<StatutSalle[]> {
    return this.http.get<StatutSalle[]>(`${this.apiUrl}/statuts`);
  }

  // Méthodes utilitaires
  getTypeLabel(type: TypeSalle): string {
    const labels: { [key in TypeSalle]: string } = {
      [TypeSalle.AMPHITHEATRE]: 'Amphitheatre',
      [TypeSalle.SALLE_COURS]: 'Lecture room',
      [TypeSalle.SALLE_COURS_AMPHITHEATRE]: 'Lecture room and amphitheatre',
      [TypeSalle.LABORATOIRE]: 'Laboratory',
      [TypeSalle.SALLE_INFORMATIQUE]: 'Computer room',
      [TypeSalle.SALLE_CONFERENCE]: 'Conference room',
      [TypeSalle.BIBLIOTHEQUE]: 'Library',
      [TypeSalle.SALLE_REUNION]: 'Meeting room'
    };
    return labels[type] || type;
  }

  getStatutLabel(statut: StatutSalle): string {
    const labels: { [key in StatutSalle]: string } = {
      [StatutSalle.DISPONIBLE]: 'Available',
      [StatutSalle.OCCUPEE]: 'Occupied',
      [StatutSalle.MAINTENANCE]: 'Under maintenance',
      [StatutSalle.HORS_SERVICE]: 'Out of service'
    };
    return labels[statut] || statut;
  }

  getStatutColor(statut: StatutSalle): string {
    const colors: { [key in StatutSalle]: string } = {
      [StatutSalle.DISPONIBLE]: 'success',
      [StatutSalle.OCCUPEE]: 'warning',
      [StatutSalle.MAINTENANCE]: 'info',
      [StatutSalle.HORS_SERVICE]: 'danger'
    };
    return colors[statut] || 'secondary';
  }
}