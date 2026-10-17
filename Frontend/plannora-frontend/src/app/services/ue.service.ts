import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UEDTO {
  id?: number;
  idUE?: string;
  codeUE: string;
  nomUE: string;
  description?: string;
  nombreCredit?: number;
  nombreHeur?: number;
  heuresCM?: number;
  heuresTD?: number;
  heuresTP?: number;
  heureDebut?: string;
  heureFin?: string;
  /** Niveau ciblé : ING1, ING2, ING3, L1, L2, M1, M2... */
  niveau?: string;
  /** Identifiant de la filière (ClassesService) */
  idFiliere?: string;
  /** Nom de la filière (dénormalisé) */
  nomFiliere?: string;
  idEcole?: string;
  idSpecialite?: string;
  specialite?: string;
}

export interface UEStatistics {
  totalUEs: number;
  totalCredits: number;
  totalHeures: number;
  avgCredits: number;
}

@Injectable({
  providedIn: 'root'
})
export class UEService {
  private apiUrl = 'http://localhost:8081/api/ues';

  constructor(private http: HttpClient) {}

  getAllUEs(): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(this.apiUrl);
  }

  getUEs(): Observable<UEDTO[]> {
    return this.getAllUEs();
  }

  getUEById(id: number): Observable<UEDTO> {
    return this.http.get<UEDTO>(`${this.apiUrl}/${id}`);
  }

  createUE(ue: UEDTO): Observable<UEDTO> {
    return this.http.post<UEDTO>(this.apiUrl, ue);
  }

  updateUE(id: number, ue: UEDTO): Observable<UEDTO> {
    return this.http.put<UEDTO>(`${this.apiUrl}/${id}`, ue);
  }

  deleteUE(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getUEsByFiliere(idFiliere: string): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(`${this.apiUrl}/filiere/${idFiliere}`);
  }

  getUEsByNiveau(niveau: string): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(`${this.apiUrl}/niveau/${niveau}`);
  }

  getUEsByFiliereAndNiveau(idFiliere: string, niveau: string): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(`${this.apiUrl}/filiere/${idFiliere}/niveau/${niveau}`);
  }

  computeStatistics(ues: UEDTO[]): UEStatistics {
    const totalCredits = ues.reduce((sum, ue) => sum + (ue.nombreCredit ?? 0), 0);
    const totalHeures = ues.reduce((sum, ue) => sum + (ue.nombreHeur ?? 0), 0);
    return {
      totalUEs: ues.length,
      totalCredits,
      totalHeures,
      avgCredits: ues.length > 0 ? totalCredits / ues.length : 0
    };
  }
}
