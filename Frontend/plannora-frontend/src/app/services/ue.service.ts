import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UEDTO {
  id?: number;
  codeUE: string;
  nomUE: string;
  description?: string;
  heuresCM: number;
  heuresTD: number;
  heuresTP: number;
  creditsECTS: number;
  semestre: string;
  niveau: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
  enseignantIds?: string[];
  enseignantNames?: string[];
  nombreEnseignants?: number;
  nombreEmploisDuTemps?: number;
}

export interface UEStatistics {
  totalUEs: number;
  totalCredits: number;
  avgCredits: number;
  totalHeures: number;
  statsBySemestre: any[];
  statsByNiveau: any[];
}

export interface UEOptions {
  semestres: string[];
  niveaux: string[];
  typesCours: string[];
}

@Injectable({
  providedIn: 'root'
})
export class UEService {
  private baseUrl = 'http://localhost:8086/api/ues';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // ========== CRUD Operations ==========

  getAllUEs(): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(this.baseUrl, {
      headers: this.getHeaders()
    });
  }

  getUEsPaginated(page: number = 0, size: number = 10, sortBy: string = 'nomUE', sortDir: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<any>(`${this.baseUrl}/paginated`, {
      headers: this.getHeaders(),
      params: params
    });
  }

  getUEById(id: number): Observable<UEDTO> {
    return this.http.get<UEDTO>(`${this.baseUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  getUEByCode(codeUE: string): Observable<UEDTO> {
    return this.http.get<UEDTO>(`${this.baseUrl}/code/${codeUE}`, {
      headers: this.getHeaders()
    });
  }

  createUE(ue: UEDTO): Observable<UEDTO> {
    return this.http.post<UEDTO>(this.baseUrl, ue, {
      headers: this.getHeaders()
    });
  }

  updateUE(id: number, ue: UEDTO): Observable<UEDTO> {
    return this.http.put<UEDTO>(`${this.baseUrl}/${id}`, ue, {
      headers: this.getHeaders()
    });
  }

  deleteUE(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {
      headers: this.getHeaders()
    });
  }

  restoreUE(id: number): Observable<UEDTO> {
    return this.http.put<UEDTO>(`${this.baseUrl}/${id}/restore`, {}, {
      headers: this.getHeaders()
    });
  }

  // ========== Search and Filter ==========

  searchUEs(searchTerm: string): Observable<UEDTO[]> {
    const params = new HttpParams().set('q', searchTerm);
    return this.http.get<UEDTO[]>(`${this.baseUrl}/search`, {
      headers: this.getHeaders(),
      params: params
    });
  }

  getUEsBySemestre(semestre: string): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(`${this.baseUrl}/semestre/${semestre}`, {
      headers: this.getHeaders()
    });
  }

  getUEsByNiveau(niveau: string): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(`${this.baseUrl}/niveau/${niveau}`, {
      headers: this.getHeaders()
    });
  }

  getUEsByEnseignant(enseignantId: string): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(`${this.baseUrl}/enseignant/${enseignantId}`, {
      headers: this.getHeaders()
    });
  }

  // ========== Enseignant Assignment ==========

  assignEnseignantToUE(ueId: number, enseignantId: string): Observable<UEDTO> {
    return this.http.post<UEDTO>(`${this.baseUrl}/${ueId}/enseignants/${enseignantId}`, {}, {
      headers: this.getHeaders()
    });
  }

  assignEnseignantToUESimple(ueId: number, enseignantId: string): Observable<UEDTO> {
    return this.http.post<UEDTO>(`${this.baseUrl}/${ueId}/enseignants/${enseignantId}/simple`, {}, {
      headers: this.getHeaders()
    });
  }

  removeEnseignantFromUE(ueId: number, enseignantId: string): Observable<UEDTO> {
    return this.http.delete<UEDTO>(`${this.baseUrl}/${ueId}/enseignants/${enseignantId}`, {
      headers: this.getHeaders()
    });
  }

  // ========== Statistics and Options ==========

  getUEStatistics(): Observable<UEStatistics> {
    return this.http.get<UEStatistics>(`${this.baseUrl}/statistics`, {
      headers: this.getHeaders()
    });
  }

  getUEOptions(): Observable<UEOptions> {
    return this.http.get<UEOptions>(`${this.baseUrl}/options`, {
      headers: this.getHeaders()
    });
  }

  // ========== Utility Methods ==========

  getTotalHeures(ue: UEDTO): number {
    return (ue.heuresCM || 0) + (ue.heuresTD || 0) + (ue.heuresTP || 0);
  }

  getTypeCoursDominant(ue: UEDTO): string {
    const cm = ue.heuresCM || 0;
    const td = ue.heuresTD || 0;
    const tp = ue.heuresTP || 0;

    if (cm >= td && cm >= tp) return 'CM';
    if (td >= tp) return 'TD';
    return 'TP';
  }

  formatUEText(ue: UEDTO): string {
    return `${ue.codeUE} - ${ue.nomUE} (${ue.creditsECTS} ECTS)`;
  }
}