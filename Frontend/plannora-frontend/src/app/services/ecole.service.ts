import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EcoleDTO {
  idEcole?: string;
  nomEcole: string;
}

@Injectable({
  providedIn: 'root'
})
export class EcoleService {
  private apiUrl = 'http://localhost:8081/api/ecoles'; // Gateway URL

  constructor(private http: HttpClient) {}

  creer(dto: EcoleDTO): Observable<EcoleDTO> {
    return this.http.post<EcoleDTO>(this.apiUrl, dto);
  }

  obtenirToutes(): Observable<EcoleDTO[]> {
    return this.http.get<EcoleDTO[]>(this.apiUrl);
  }

  obtenirParId(idEcole: string): Observable<EcoleDTO> {
    return this.http.get<EcoleDTO>(`${this.apiUrl}/${idEcole}`);
  }

  mettreAJour(idEcole: string, dto: EcoleDTO): Observable<EcoleDTO> {
    return this.http.put<EcoleDTO>(`${this.apiUrl}/${idEcole}`, dto);
  }

  supprimer(idEcole: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${idEcole}`);
  }
}
