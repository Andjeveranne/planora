import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface StudentCourse {
  id: number;
  code: string;
  label: string;
  teacher: string;
  room: string;
  type: 'CM' | 'TD' | 'TP';
  day: string;
  time: string;
  attendance: number;
  color: string;
}

export interface StudentSession {
  id: number;
  title: string;
  date: string;
  time: string;
  room: string;
  type: string;
}

export interface AttendanceItem {
  id: number;
  course: string;
  presence: number;
  total: number;
  status: 'Présent' | 'Absent' | 'Rappel';
}

export interface StudentDashboardData {
  overview: {
    totalCourses: number;
    upcomingSessions: number;
    attendanceRate: number;
    credits: number;
  };
  courses: StudentCourse[];
  sessions: StudentSession[];
  attendance: AttendanceItem[];
}

export interface StudentProfile {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  password?: string;
  school: string;
  department: string;
  specialty: string;
  level: string;
  status: 'Actif' | 'En pause';
}

export interface StudentApiResponse {
  idUser: string;
  email: string;
  nomUser: string;
  prenomUser: string;
  telephone: string;
  role: string;
  idEcole?: string;
  departement?: string;
  specialite?: string;
  niveau?: string;
  classe?: string;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly storageKey = 'planora-students';
  private readonly apiUrl = 'http://localhost:8081/api/etudiants';
  private readonly authApiUrl = 'http://localhost:8081/api/auth';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return token ? headers.set('Authorization', `Bearer ${token}`) : headers;
  }

  private normalizeEmail(email: string): string {
    return (email || '').trim().toLowerCase();
  }

  private buildInstitutionalEmail(student: Pick<StudentProfile, 'prenom' | 'nom'>): string {
    const prenom = (student.prenom || '').trim().toLowerCase().replace(/[^a-z]/g, '');
    const nom = (student.nom || '').trim().toLowerCase().replace(/[^a-z]/g, '');

    if (!prenom || !nom) {
      return '';
    }

    return `${prenom}.${nom}@ictuniversity.org`;
  }

  private buildCreatePayload(student: StudentProfile) {
    const password = student.password && student.password.trim() ? student.password.trim() : 'student123';
    const email = this.normalizeEmail(student.email || this.buildInstitutionalEmail(student));

    return {
      email,
      mdp: password,
      nomUser: student.nom,
      prenomUser: student.prenom,
      telephone: '0000000000',
      role: 'ETUDIANT',
      idEcole: student.school,
      departement: student.department,
      specialite: student.specialty,
      niveau: student.level,
      classe: student.level,
      status: student.status
    };
  }

  getDashboardData(): StudentDashboardData {
    return {
      overview: {
        totalCourses: 7,
        upcomingSessions: 4,
        attendanceRate: 92,
        credits: 28
      },
      courses: [
        { id: 1, code: 'INFO-101', label: 'Algorithmique et structures de données', teacher: 'Dr. Nde', room: 'A-204', type: 'CM', day: 'Lundi', time: '08:00 - 10:00', attendance: 96, color: '#4f46e5' },
        { id: 2, code: 'INFO-205', label: 'Programmation orientée objet', teacher: 'M. Tchinda', room: 'B-112', type: 'TD', day: 'Mardi', time: '10:00 - 12:00', attendance: 88, color: '#0ea5e9' },
        { id: 3, code: 'MATH-120', label: 'Analyse mathématique', teacher: 'Pr. Ndzi', room: 'C-301', type: 'CM', day: 'Mercredi', time: '14:00 - 16:00', attendance: 94, color: '#14b8a6' },
        { id: 4, code: 'INFO-310', label: 'Base de données avancées', teacher: 'Dr. Ewane', room: 'Lab 2', type: 'TP', day: 'Jeudi', time: '09:00 - 12:00', attendance: 90, color: '#f59e0b' },
        { id: 5, code: 'RES-240', label: 'Réseaux et sécurité', teacher: 'M. Mballa', room: 'A-118', type: 'TD', day: 'Vendredi', time: '08:00 - 10:00', attendance: 85, color: '#ef4444' },
        { id: 6, code: 'COM-150', label: 'Communication professionnelle', teacher: 'Mme. Abia', room: 'D-205', type: 'CM', day: 'Samedi', time: '09:00 - 11:00', attendance: 89, color: '#8b5cf6' }
      ],
      sessions: [
        { id: 1, title: 'Séance de projet logiciel', date: '2026-10-23', time: '08:00 - 10:00', room: 'Lab 2', type: 'TP' },
        { id: 2, title: 'Cours de système d’exploitation', date: '2026-10-24', time: '10:00 - 12:00', room: 'B-112', type: 'TD' },
        { id: 3, title: 'Visio cours de gestion de projet', date: '2026-10-25', time: '14:00 - 16:00', room: 'Virtual', type: 'CM' },
        { id: 4, title: 'Atelier de sécurité informatique', date: '2026-10-27', time: '09:00 - 11:00', room: 'A-118', type: 'Atelier' }
      ],
      attendance: [
        { id: 1, course: 'Algorithmique', presence: 18, total: 20, status: 'Présent' },
        { id: 2, course: 'Programmation orientée objet', presence: 16, total: 18, status: 'Présent' },
        { id: 3, course: 'Base de données', presence: 14, total: 16, status: 'Rappel' },
        { id: 4, course: 'Réseaux', presence: 12, total: 15, status: 'Absent' }
      ]
    };
  }

  getStudentDirectory(): StudentProfile[] {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return [];
    }

    try {
      return JSON.parse(raw) as StudentProfile[];
    } catch {
      return [];
    }
  }

  getStudentsFromApi(): Observable<StudentProfile[]> {
    return this.http
      .get<StudentApiResponse[]>(this.apiUrl, { headers: this.getHeaders() })
      .pipe(map((students) => students.map((student) => this.toStudentProfile(student))));
  }

  createStudent(student: StudentProfile): Observable<StudentProfile> {
    const payload = this.buildCreatePayload(student);

    return this.http
      .post<StudentApiResponse>(this.apiUrl, payload, { headers: this.getHeaders() })
      .pipe(map((response) => this.toStudentProfile(response)));
  }

  registerAuthStudent(student: StudentProfile): Observable<any> {
    const email = this.normalizeEmail(student.email || this.buildInstitutionalEmail(student));
    const password = student.password && student.password.trim() ? student.password.trim() : 'student123';

    return this.http.post(
      `${this.authApiUrl}/register`,
      {
        email,
        password,
        nom: student.nom,
        prenom: student.prenom,
        role: 'ETUDIANT',
        telephone: '0000000000'
      },
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  updateStudent(id: string, student: StudentProfile): Observable<StudentProfile> {
    const payload = this.buildCreatePayload(student);

    return this.http
      .put<StudentApiResponse>(`${this.apiUrl}/${id}`, payload, { headers: this.getHeaders() })
      .pipe(map((response) => this.toStudentProfile(response)));
  }

  deleteStudent(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  saveStudentDirectory(students: StudentProfile[]): void {
    localStorage.setItem(this.storageKey, JSON.stringify(students));
  }

  private toStudentProfile(student: StudentApiResponse): StudentProfile {
    return {
      id: student.idUser,
      nom: student.nomUser || '',
      prenom: student.prenomUser || '',
      email: student.email || '',
      school: student.idEcole || '',
      department: student.departement || '',
      specialty: student.specialite || '',
      level: student.niveau || 'L3',
      status: (student.status === 'En pause' ? 'En pause' : 'Actif') as 'Actif' | 'En pause'
    };
  }
}
