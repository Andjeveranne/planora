import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TeacherNotification {
  id: number;
  destinataireId: string;
  canal: 'IN_APP' | 'EMAIL' | 'SMS';
  sujet: string;
  message: string;
  statut: 'NON_LU' | 'LU';
  dateEnvoi: string;
  scheduledAt?: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationApiService {
  private readonly baseUrl = 'http://localhost:8081/api/notifications';

  constructor(private http: HttpClient) {}

  getForTeacher(teacherId: string): Observable<TeacherNotification[]> {
    return this.http.get<TeacherNotification[]>(`${this.baseUrl}/utilisateur/${encodeURIComponent(teacherId)}`);
  }

  markRead(id: number): Observable<TeacherNotification> {
    return this.http.put<TeacherNotification>(`${this.baseUrl}/${id}/lire`, {});
  }

  markAllRead(teacherId: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/utilisateur/${encodeURIComponent(teacherId)}/lire-tout`, {});
  }
}
