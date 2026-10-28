import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';

export interface DisponibiliteDTO {
    id?: number;
    enseignantId: string;
    idEcole: string;
    dateHeureDebut: string; // ISO String
    dateHeureFin: string;   // ISO String
    commentaire?: string;
}

export interface EmploiDuTempsDTO {
    id?: number;
    enseignantId: string;
    dateHeureDebut?: string;
    dateHeureFin?: string;
    dateJour?: string;
    jour?: string;
    heureDebut?: number;
    heureFin?: number;
    ue?: string;
    classe?: string;
    salle?: string;
    classeId?: string;
    nomClasse?: string;
    idEcole?: string;
    ueId?: number;
    codeUE?: string;
    nomUE?: string;
    salleId?: string;
    nomSalle?: string;
    typeCours: string;
    semaine?: number;
    jourSemaine?: number;
    creneau?: string;
}

@Injectable({
    providedIn: 'root'
})
export class PlanningService {
    private baseUrl = 'http://localhost:8081/api/planning';
    private dispoUrl = `${this.baseUrl}/disponibilites`;
    private fallbackDispoUrl = 'http://localhost:8081/api/disponibilites';
    private edtUrl = `${this.baseUrl}/edt`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    private fallbackRequest<T>(planningUrl: string, fallbackUrl: string): Observable<T> {
        return this.http.get<T>(planningUrl, { headers: this.getHeaders() }).pipe(
            catchError(() => this.http.get<T>(fallbackUrl, { headers: this.getHeaders() }))
        );
    }

    private fallbackMutation<T>(planningUrl: string, fallbackUrl: string, body: any): Observable<T> {
        return this.http.post<T>(planningUrl, body, { headers: this.getHeaders() }).pipe(
            catchError(() => this.http.post<T>(fallbackUrl, body, { headers: this.getHeaders() }))
        );
    }

    // ========== DISPONIBILITÉS ==========

    getDisponibilites(enseignantId: string): Observable<DisponibiliteDTO[]> {
        const planningUrl = `${this.dispoUrl}/enseignant/${enseignantId}`;
        const fallbackUrl = `${this.fallbackDispoUrl}/enseignant/${enseignantId}`;
        return this.fallbackRequest<DisponibiliteDTO[]>(planningUrl, fallbackUrl).pipe(
            catchError(() => of([]))
        );
    }

    addDisponibilite(dispo: DisponibiliteDTO): Observable<DisponibiliteDTO> {
        const planningUrl = this.dispoUrl;
        const fallbackUrl = this.fallbackDispoUrl;
        return this.fallbackMutation<DisponibiliteDTO>(planningUrl, fallbackUrl, dispo).pipe(
            catchError(() => of(dispo))
        );
    }

    deleteDisponibilite(id: number): Observable<void> {
        const planningUrl = `${this.dispoUrl}/${id}`;
        const fallbackUrl = `${this.fallbackDispoUrl}/${id}`;
        return this.http.delete<void>(planningUrl, { headers: this.getHeaders() }).pipe(
            catchError(() => this.http.delete<void>(fallbackUrl, { headers: this.getHeaders() }))
        );
    }

    // ========== EMPLOI DU TEMPS ==========

    getEdtByEnseignant(enseignantId: string): Observable<EmploiDuTempsDTO[]> {
        return this.http.get<EmploiDuTempsDTO[]>(`${this.edtUrl}/enseignant/${enseignantId}`, {
            headers: this.getHeaders()
        });
    }

    getEdtByEnseignantAndWeek(enseignantId: string, dateDebut: string, dateFin: string): Observable<EmploiDuTempsDTO[]> {
        const week = this.isoWeekNumber(new Date(`${dateDebut}T12:00:00`));
        return this.http.get<EmploiDuTempsDTO[]>(`${this.baseUrl}/emplois-du-temps/enseignant/${enseignantId}`, {
            headers: this.getHeaders(),
            params: { semaine: week }
        });
    }

    genererEdt(enseignantId: string): Observable<EmploiDuTempsDTO[]> {
        return this.http.post<EmploiDuTempsDTO[]>(`${this.edtUrl}/generer/${enseignantId}`, {}, {
            headers: this.getHeaders()
        });
    }

    // Génération intelligente d'emploi du temps
    genererEdtIntelligent(enseignantId: string, nombreSemaines: number = 4): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/emplois-du-temps/generer-intelligent/${enseignantId}?nombreSemaines=${nombreSemaines}`, {}, {
            headers: this.getHeaders()
        });
    }

    // Récupérer l'emploi du temps depuis PlanningService
    getEdtFromUserService(enseignantId: string): Observable<EmploiDuTempsDTO[]> {
        return this.http.get<EmploiDuTempsDTO[]>(`${this.baseUrl}/emplois-du-temps/enseignant/${enseignantId}`, {
            headers: this.getHeaders()
        });
    }

    private isoWeekNumber(date: Date): number {
        const utcDate = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
        const day = utcDate.getUTCDay() || 7;
        utcDate.setUTCDate(utcDate.getUTCDate() + 4 - day);
        const yearStart = new Date(Date.UTC(utcDate.getUTCFullYear(), 0, 1));
        return Math.ceil((((utcDate.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
    }

    deleteEdtByEnseignant(enseignantId: string): Observable<void> {
        return this.http.delete<void>(`${this.edtUrl}/enseignant/${enseignantId}`, {
            headers: this.getHeaders()
        });
    }

    deleteCours(id: number): Observable<void> {
        return this.http.delete<void>(`${this.edtUrl}/${id}`, {
            headers: this.getHeaders()
        });
    }
}
