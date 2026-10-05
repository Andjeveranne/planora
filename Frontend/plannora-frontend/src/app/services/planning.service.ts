import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

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
    private edtUrl = `${this.baseUrl}/edt`;

    constructor(private http: HttpClient) { }

    private getHeaders(): HttpHeaders {
        const token = localStorage.getItem('token');
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        });
    }

    // ========== DISPONIBILITÉS ==========

    getDisponibilites(enseignantId: string): Observable<DisponibiliteDTO[]> {
        return this.http.get<DisponibiliteDTO[]>(`${this.dispoUrl}/enseignant/${enseignantId}`, {
            headers: this.getHeaders()
        });
    }

    addDisponibilite(dispo: DisponibiliteDTO): Observable<DisponibiliteDTO> {
        return this.http.post<DisponibiliteDTO>(this.dispoUrl, dispo, {
            headers: this.getHeaders()
        });
    }

    deleteDisponibilite(id: number): Observable<void> {
        return this.http.delete<void>(`${this.dispoUrl}/${id}`, {
            headers: this.getHeaders()
        });
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
