import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface FiliereDTO {
  idFiliere?: string;
  nom: string;
  description?: string;
  idEcole?: string;
  classes?: ClasseDTO[];
}

export interface ClasseDTO {
  idClasse?: string;
  nomClasse: string;
  effectifs: number;
  niveau: string;
  idFiliere: string;
  specialite?: string;
  nomFiliere?: string;
  idEcole?: string;
}

export interface SpecialiteDTO {
  idSpecialite?: string;
  nom: string;
  description?: string;
  idFiliere: string;
  nomDepartement?: string;
  idEcole?: string;
}

@Injectable({ providedIn: 'root' })
export class ClassesService {
  private classesUrl = 'http://localhost:8081/api/classes';
  private filieresUrl = 'http://localhost:8081/api/filieres';
  private specialitesUrl = 'http://localhost:8081/api/specialites';

  constructor(private http: HttpClient) {}

  // Filières
  getFilieres(): Observable<FiliereDTO[]> {
    return this.http.get<FiliereDTO[]>(this.filieresUrl);
  }

  getFiliereById(id: string): Observable<FiliereDTO> {
    return this.http.get<FiliereDTO>(`${this.filieresUrl}/${id}`);
  }

  createFiliere(f: FiliereDTO): Observable<FiliereDTO> {
    return this.http.post<FiliereDTO>(this.filieresUrl, f);
  }

  updateFiliere(id: string, f: FiliereDTO): Observable<FiliereDTO> {
    return this.http.put<FiliereDTO>(`${this.filieresUrl}/${id}`, f);
  }

  deleteFiliere(id: string): Observable<void> {
    return this.http.delete<void>(`${this.filieresUrl}/${id}`);
  }

  getSpecialites(): Observable<SpecialiteDTO[]> {
    return this.http.get<SpecialiteDTO[]>(this.specialitesUrl);
  }

  createSpecialite(specialite: SpecialiteDTO): Observable<SpecialiteDTO> {
    return this.http.post<SpecialiteDTO>(this.specialitesUrl, specialite);
  }

  updateSpecialite(id: string, specialite: SpecialiteDTO): Observable<SpecialiteDTO> {
    return this.http.put<SpecialiteDTO>(`${this.specialitesUrl}/${id}`, specialite);
  }

  deleteSpecialite(id: string): Observable<void> {
    return this.http.delete<void>(`${this.specialitesUrl}/${id}`);
  }

  // Classes
  getClasses(): Observable<ClasseDTO[]> {
    return this.http.get<ClasseDTO[]>(this.classesUrl);
  }

  getClassesByFiliere(idFiliere: string): Observable<ClasseDTO[]> {
    return this.http.get<ClasseDTO[]>(`${this.classesUrl}/filiere/${idFiliere}`);
  }

  createClasse(c: ClasseDTO): Observable<ClasseDTO> {
    return this.http.post<ClasseDTO>(this.classesUrl, c);
  }

  updateClasse(id: string, c: ClasseDTO): Observable<ClasseDTO> {
    return this.http.put<ClasseDTO>(`${this.classesUrl}/${id}`, c);
  }

  deleteClasse(id: string): Observable<void> {
    return this.http.delete<void>(`${this.classesUrl}/${id}`);
  }
}
