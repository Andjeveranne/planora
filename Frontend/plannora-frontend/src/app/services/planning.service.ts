import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DisponibiliteDTO {
    id?: number;
    enseignantId: string;
    dateHeureDebut: string; // ISO String
    dateHeureFin: string;   // ISO String
    commentaire?: string;
}

export interface EmploiDuTempsDTO {
    id?: number;
    enseignantId: string;
    dateJour: string; // ISO Date (YYYY-MM-DD)
    jour: string;
    heureDebut: number;
    heureFin: number;
    ue: string;
    classe: string;
    salle: string;
    typeCours: string;
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
        return this.http.get<EmploiDuTempsDTO[]>(
            `${this.edtUrl}/enseignant/${enseignantId}/semaine?dateDebut=${dateDebut}&dateFin=${dateFin}`, {
            headers: this.getHeaders()
        });
    }

    genererEdt(enseignantId: string): Observable<EmploiDuTempsDTO[]> {
        return this.http.post<EmploiDuTempsDTO[]>(`${this.edtUrl}/generer/${enseignantId}`, {}, {
            headers: this.getHeaders()
        });
    }

    // Génération intelligente d'emploi du temps via UserService
    genererEdtIntelligent(enseignantId: string, nombreSemaines: number = 4): Observable<any> {
        const userServiceUrl = 'http://localhost:8081/api/emplois-du-temps';
        return this.http.post<any>(`${userServiceUrl}/generer-intelligent/${enseignantId}?nombreSemaines=${nombreSemaines}`, {}, {
            headers: this.getHeaders()
        });
    }

    // Récupérer l'emploi du temps depuis UserService
    getEdtFromUserService(enseignantId: string): Observable<EmploiDuTempsDTO[]> {
        const userServiceUrl = 'http://localhost:8081/api/emplois-du-temps';
        return this.http.get<EmploiDuTempsDTO[]>(`${userServiceUrl}/enseignant/${enseignantId}`, {
            headers: this.getHeaders()
        });
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
