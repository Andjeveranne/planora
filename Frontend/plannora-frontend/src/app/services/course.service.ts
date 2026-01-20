import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CourseDTO {
  id?: number;
  codeCourse: string;
  typeCours: string;
  nombreHeures: number;
  semestre: string;
  description?: string;
  active?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  
  // Relations
  ueId: number;
  enseignantId: string;
  classeId: number;
  
  // Informations détaillées
  ueCode?: string;
  ueNom?: string;
  enseignantNom?: string;
  enseignantPrenom?: string;
  classeNom?: string;
  classeNiveau?: string;
  
  // Informations calculées
  nombreCreneaux?: number;
  heuresProgrammees?: number;
  completementProgramme?: boolean;
  emploiDuTempsIds?: number[];
}

export interface ClasseDTO {
  id?: number;
  nomClasse: string;
  niveau: string;
  filiere: string;
  anneeAcademique: string;
  nombreEtudiants: number;
  description?: string;
  active?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  
  // Informations calculées
  nombreCours?: number;
  totalHeuresCours?: number;
  totalCreditsECTS?: number;
  courseIds?: number[];
}

export interface CourseStats {
  totalCourses: number;
  totalHeures: number;
  statsByEnseignant: { [key: string]: number };
  statsByClasse: { [key: string]: number };
}

export interface ClasseStats {
  totalClasses: number;
  totalEtudiants: number;
  statsByNiveau: { [key: string]: number };
  statsByFiliere: { [key: string]: number };
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private baseUrl = 'http://localhost:8082/api';

  constructor(private http: HttpClient) {}

  // ===== COURSE METHODS =====

  /**
   * Récupère tous les cours
   */
  getAllCourses(): Observable<CourseDTO[]> {
    return this.http.get<CourseDTO[]>(`${this.baseUrl}/courses`);
  }

  /**
   * Récupère un cours par son ID
   */
  getCourseById(id: number): Observable<CourseDTO> {
    return this.http.get<CourseDTO>(`${this.baseUrl}/courses/${id}`);
  }

  /**
   * Récupère les cours par enseignant
   */
  getCoursesByEnseignant(enseignantId: string): Observable<CourseDTO[]> {
    return this.http.get<CourseDTO[]>(`${this.baseUrl}/courses/enseignant/${enseignantId}`);
  }

  /**
   * Récupère les cours par classe
   */
  getCoursesByClasse(classeId: number): Observable<CourseDTO[]> {
    return this.http.get<CourseDTO[]>(`${this.baseUrl}/courses/classe/${classeId}`);
  }

  /**
   * Récupère les cours par enseignant et classe
   */
  getCoursesByEnseignantAndClasse(enseignantId: string, classeId: number): Observable<CourseDTO[]> {
    return this.http.get<CourseDTO[]>(`${this.baseUrl}/courses/enseignant/${enseignantId}/classe/${classeId}`);
  }

  /**
   * Récupère les classes enseignées par un enseignant
   */
  getClassesByEnseignant(enseignantId: string): Observable<ClasseDTO[]> {
    return this.http.get<ClasseDTO[]>(`${this.baseUrl}/courses/enseignant/${enseignantId}/classes`);
  }

  /**
   * Recherche des cours
   */
  searchCourses(searchTerm: string): Observable<CourseDTO[]> {
    const params = new HttpParams().set('q', searchTerm);
    return this.http.get<CourseDTO[]>(`${this.baseUrl}/courses/search`, { params });
  }

  /**
   * Crée un nouveau cours
   */
  createCourse(course: CourseDTO): Observable<CourseDTO> {
    return this.http.post<CourseDTO>(`${this.baseUrl}/courses`, course);
  }

  /**
   * Met à jour un cours
   */
  updateCourse(id: number, course: CourseDTO): Observable<CourseDTO> {
    return this.http.put<CourseDTO>(`${this.baseUrl}/courses/${id}`, course);
  }

  /**
   * Supprime un cours
   */
  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/courses/${id}`);
  }

  /**
   * Récupère les statistiques des cours
   */
  getCourseStats(): Observable<CourseStats> {
    return this.http.get<CourseStats>(`${this.baseUrl}/courses/stats`);
  }

  // ===== CLASSE METHODS =====

  /**
   * Récupère toutes les classes
   */
  getAllClasses(): Observable<ClasseDTO[]> {
    return this.http.get<ClasseDTO[]>(`${this.baseUrl}/classes`);
  }

  /**
   * Récupère une classe par son ID
   */
  getClasseById(id: number): Observable<ClasseDTO> {
    return this.http.get<ClasseDTO>(`${this.baseUrl}/classes/${id}`);
  }

  /**
   * Récupère les classes par niveau
   */
  getClassesByNiveau(niveau: string): Observable<ClasseDTO[]> {
    return this.http.get<ClasseDTO[]>(`${this.baseUrl}/classes/niveau/${niveau}`);
  }

  /**
   * Récupère les classes par filière
   */
  getClassesByFiliere(filiere: string): Observable<ClasseDTO[]> {
    return this.http.get<ClasseDTO[]>(`${this.baseUrl}/classes/filiere/${filiere}`);
  }

  /**
   * Recherche des classes
   */
  searchClasses(searchTerm: string): Observable<ClasseDTO[]> {
    const params = new HttpParams().set('q', searchTerm);
    return this.http.get<ClasseDTO[]>(`${this.baseUrl}/classes/search`, { params });
  }

  /**
   * Crée une nouvelle classe
   */
  createClasse(classe: ClasseDTO): Observable<ClasseDTO> {
    return this.http.post<ClasseDTO>(`${this.baseUrl}/classes`, classe);
  }

  /**
   * Met à jour une classe
   */
  updateClasse(id: number, classe: ClasseDTO): Observable<ClasseDTO> {
    return this.http.put<ClasseDTO>(`${this.baseUrl}/classes/${id}`, classe);
  }

  /**
   * Supprime une classe
   */
  deleteClasse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/classes/${id}`);
  }

  /**
   * Récupère les statistiques des classes
   */
  getClasseStats(): Observable<ClasseStats> {
    return this.http.get<ClasseStats>(`${this.baseUrl}/classes/stats`);
  }

  // ===== UTILITY METHODS =====

  /**
   * Formate le nom complet d'un cours
   */
  formatCourseNomComplet(course: CourseDTO): string {
    if (course.ueCode && course.ueNom) {
      return `${course.ueCode} - ${course.ueNom} (${course.typeCours})`;
    }
    return `${course.codeCourse} (${course.typeCours})`;
  }

  /**
   * Formate les informations de l'enseignant
   */
  formatEnseignantInfo(course: CourseDTO): string {
    if (course.enseignantPrenom && course.enseignantNom) {
      return `${course.enseignantPrenom} ${course.enseignantNom}`;
    }
    return 'Enseignant non défini';
  }

  /**
   * Formate les informations de la classe
   */
  formatClasseInfo(course: CourseDTO): string {
    if (course.classeNom) {
      return course.classeNiveau ? 
        `${course.classeNom} (${course.classeNiveau})` : course.classeNom;
    }
    return 'Classe non définie';
  }

  /**
   * Vérifie si un cours est valide
   */
  isCourseValid(course: CourseDTO): boolean {
    return !!(course.codeCourse && course.codeCourse.trim() &&
             course.typeCours && course.typeCours.trim() &&
             course.nombreHeures && course.nombreHeures > 0 &&
             course.semestre && course.semestre.trim() &&
             course.ueId && course.enseignantId && course.classeId);
  }

  /**
   * Vérifie si une classe est valide
   */
  isClasseValid(classe: ClasseDTO): boolean {
    return !!(classe.nomClasse && classe.nomClasse.trim() &&
             classe.niveau && classe.niveau.trim() &&
             classe.filiere && classe.filiere.trim() &&
             classe.anneeAcademique && classe.anneeAcademique.trim() &&
             classe.nombreEtudiants && classe.nombreEtudiants > 0);
  }
}