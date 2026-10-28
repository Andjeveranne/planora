import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EmploiDuTempsDTO {
  id?: number;
  classeId: string;
  nomClasse?: string;
  enseignantId: string;
  idEcole: string;
  nomEnseignant?: string;
  ueId: number;
  codeUE?: string;
  nomUE?: string;
  salleId: string;
  nomSalle?: string;
  dateHeureDebut: string;
  dateHeureFin: string;
  typeCours: string;
  semaine: number;
  jourSemaine: number;
  creneau: string;
}

export interface UEClasseDTO {
  id?: number;
  ueId: number;
  codeUE?: string;
  nomUE?: string;
  classeId: string;
  nomClasse?: string;
  heuresCM: number;
  heuresTD: number;
  heuresTP: number;
}

export interface UEEnseignantDTO {
  id?: number;
  ueId: number;
  codeUE?: string;
  nomUE?: string;
  enseignantId: string;
  idEcole?: string;
  peutCM: boolean;
  peutTD: boolean;
  peutTP: boolean;
  preference?: number;
  commentaire?: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmploiDuTempsService {
  private baseUrl = 'http://localhost:8081/api/planning';

  constructor(private http: HttpClient) {}

  // Génération d'emplois du temps pour la semaine à venir
  genererEmploisDuTemps(idEcole?: string, departement?: string, specialite?: string): Observable<EmploiDuTempsDTO[]> {
    let params = new HttpParams();
    if (idEcole) params = params.set('idEcole', idEcole);
    if (departement) params = params.set('departement', departement);
    if (specialite) params = params.set('specialite', specialite);
    return this.http.post<EmploiDuTempsDTO[]>(`${this.baseUrl}/emplois-du-temps/generer`, null, { params });
  }

  // Nouvelle version avec algorithme amélioré
  genererEmploisDuTempsV2(idEcole: string): Observable<EmploiDuTempsDTO[]> {
    return this.http.post<EmploiDuTempsDTO[]>(`${this.baseUrl}/emplois-du-temps/generer-v2`, { idEcole });
  }

  // Enseignants ayant soumis des disponibilités
  getEnseignantsAvecDisponibilites(idEcole?: string, departement?: string, specialite?: string): Observable<any[]> {
    let params = new HttpParams();
    if (idEcole) params = params.set('idEcole', idEcole);
    if (departement) params = params.set('departement', departement);
    if (specialite) params = params.set('specialite', specialite);
    return this.http.get<any[]>(`${this.baseUrl}/emplois-du-temps/enseignants-disponibles`, { params });
  }

  // Récupérer les disponibilités d'un enseignant
  getDisponibilitesEnseignant(enseignantId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/disponibilites/enseignant/${enseignantId}`);
  }

  // Récupération d'emplois du temps
  getEmploiDuTempsClasse(classeId: string, semaine?: number): Observable<EmploiDuTempsDTO[]> {
    let params = new HttpParams();
    if (semaine) {
      params = params.set('semaine', semaine.toString());
    }
    
    return this.http.get<EmploiDuTempsDTO[]>(`${this.baseUrl}/emplois-du-temps/classe/${classeId}`, { params });
  }

  getEmploiDuTempsEnseignant(enseignantId: string, semaine?: number): Observable<EmploiDuTempsDTO[]> {
    let params = new HttpParams();
    if (semaine) {
      params = params.set('semaine', semaine.toString());
    }
    
    return this.http.get<EmploiDuTempsDTO[]>(`${this.baseUrl}/emplois-du-temps/enseignant/${enseignantId}`, { params });
  }

  supprimerEmploisDuTemps(semaine: number, idEcole: string): Observable<void> {
    const params = new HttpParams()
      .set('semaine', semaine.toString())
      .set('idEcole', idEcole);
    
    return this.http.delete<void>(`${this.baseUrl}/emplois-du-temps/supprimer`, { params });
  }

  // Gestion UE-Classe
  creerUEClasse(ueClasse: UEClasseDTO): Observable<UEClasseDTO> {
    return this.http.post<UEClasseDTO>(`${this.baseUrl}/ue-classes`, ueClasse);
  }

  getUEClasses(): Observable<UEClasseDTO[]> {
    return this.http.get<UEClasseDTO[]>(`${this.baseUrl}/ue-classes`);
  }

  getUEClassesParClasse(classeId: string): Observable<UEClasseDTO[]> {
    return this.http.get<UEClasseDTO[]>(`${this.baseUrl}/ue-classes/classe/${classeId}`);
  }

  getUEClassesParUE(ueId: number): Observable<UEClasseDTO[]> {
    return this.http.get<UEClasseDTO[]>(`${this.baseUrl}/ue-classes/ue/${ueId}`);
  }

  mettreAJourUEClasse(id: number, ueClasse: UEClasseDTO): Observable<UEClasseDTO> {
    return this.http.put<UEClasseDTO>(`${this.baseUrl}/ue-classes/${id}`, ueClasse);
  }

  supprimerUEClasse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/ue-classes/${id}`);
  }

  // Gestion UE-Enseignant
  creerUEEnseignant(ueEnseignant: UEEnseignantDTO): Observable<UEEnseignantDTO> {
    return this.http.post<UEEnseignantDTO>('http://localhost:8081/api/enseignant-ues', ueEnseignant);
  }

  ajouterUEEnseignant(ueEnseignant: UEEnseignantDTO): Observable<UEEnseignantDTO> {
    return this.creerUEEnseignant(ueEnseignant);
  }

  getUEEnseignants(): Observable<UEEnseignantDTO[]> {
    return this.http.get<UEEnseignantDTO[]>(`${this.baseUrl}/ue-enseignants`);
  }

  getUEEnseignantsParEnseignant(enseignantId: string): Observable<UEEnseignantDTO[]> {
    return this.http.get<UEEnseignantDTO[]>(`http://localhost:8081/api/enseignant-ues/enseignant/${enseignantId}`);
  }

  getUEEnseignantsParUE(ueId: number): Observable<UEEnseignantDTO[]> {
    return this.http.get<UEEnseignantDTO[]>(`http://localhost:8081/api/enseignant-ues/ue/${ueId}`);
  }

  mettreAJourUEEnseignant(id: number, ueEnseignant: UEEnseignantDTO): Observable<UEEnseignantDTO> {
    return this.http.put<UEEnseignantDTO>(`http://localhost:8081/api/enseignant-ues/${id}`, ueEnseignant);
  }

  supprimerUEEnseignant(id: number): Observable<void> {
    return this.http.delete<void>(`http://localhost:8081/api/enseignant-ues/${id}`);
  }
}