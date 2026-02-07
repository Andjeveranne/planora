import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * DTO conforme au diagramme de classes : IdUE, CodeUE, NomUE, Quantite, Description, NCredit.
 */
export interface UEDTO {
  idUE?: string;
  codeUE: string;
  nomUE: string;
  quantite?: number;
  description?: string;
  nCredit?: number;
}

/** Statistiques calculées côté client à partir de la liste des UEs. */
export interface UEStatistics {
  totalUEs: number;
  totalCredits: number;
  avgCredits: number;
  totalQuantite: number;
}

@Injectable({
  providedIn: 'root'
})
export class UEService {
  private baseUrl = 'http://localhost:8081/api/ues';

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getAllUEs(): Observable<UEDTO[]> {
    return this.http.get<UEDTO[]>(this.baseUrl, {
      headers: this.getHeaders()
    });
  }

  getUEById(idUE: string): Observable<UEDTO> {
    return this.http.get<UEDTO>(`${this.baseUrl}/${idUE}`, {
      headers: this.getHeaders()
    });
  }

  createUE(ue: UEDTO): Observable<UEDTO> {
    return this.http.post<UEDTO>(this.baseUrl, ue, {
      headers: this.getHeaders()
    });
  }

  updateUE(idUE: string, ue: UEDTO): Observable<UEDTO> {
    return this.http.put<UEDTO>(`${this.baseUrl}/${idUE}`, ue, {
      headers: this.getHeaders()
    });
  }

  deleteUE(idUE: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${idUE}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Calcule les statistiques à partir de la liste des UEs (côté client).
   */
  computeStatistics(ues: UEDTO[]): UEStatistics {
    const totalUEs = ues.length;
    const totalCredits = ues.reduce((sum, u) => sum + (u.nCredit ?? 0), 0);
    const totalQuantite = ues.reduce((sum, u) => sum + (u.quantite ?? 0), 0);
    return {
      totalUEs,
      totalCredits,
      avgCredits: totalUEs > 0 ? totalCredits / totalUEs : 0,
      totalQuantite
    };
  }

  formatUEText(ue: UEDTO): string {
    const credits = ue.nCredit != null ? ` (${ue.nCredit} cr.)` : '';
    return `${ue.codeUE} - ${ue.nomUE}${credits}`;
  }
}
