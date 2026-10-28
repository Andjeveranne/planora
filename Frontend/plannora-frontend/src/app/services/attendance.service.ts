import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { CourseDTO, CourseService } from './course.service';
import { UEDTO, UEService } from './ue.service';

export interface CourseCatalogItem {
  id: string;
  code: string;
  title: string;
  specialty: string;
  department: string;
  school: string;
  teacherId: string;
  teacherName: string;
  credits: number;
  type: 'CM' | 'TD' | 'TP';
  room: string;
  schedule: string;
  color: string;
  enrolled: boolean;
  day?: string;
  startTime?: string;
  endTime?: string;
  description?: string;
}

export interface AttendanceSession {
  id: string;
  courseId: string;
  courseCode: string;
  title: string;
  teacherId: string;
  teacherName: string;
  startedAt: string;
  expiresAt: string;
  isActive: boolean;
  qrToken: string;
  scannedBy: string[];
  room?: string;
  sessionType?: string;
  attendeesCount?: number;
  enrolledCount?: number;
  remainingSeconds?: number;
}

export interface AttendanceRecord {
  id: string;
  sessionId: string;
  studentId: string;
  studentName: string;
  studentEmail?: string;
  courseId: string;
  courseCode?: string;
  courseTitle?: string;
  status: 'present' | 'late' | 'absent' | 'PRESENT' | 'LATE' | 'ABSENT';
  scannedAt: string;
  manualOverride?: boolean;
}

export interface TeacherStudentRecord {
  studentId: string;
  name: string;
  email: string;
  school: string;
  department: string;
  specialty: string;
  course: string;
  attendanceRate: number;
  status: 'Present' | 'Late' | 'Absent';
}

export interface StudentCalendarEvent {
  id: string;
  title: string;
  courseId: string;
  start: string;
  end: string;
  type: 'CM' | 'TD' | 'TP';
  room: string;
  school: string;
  teacherName: string;
  color: string;
}

export interface StudentDashboardSnapshot {
  overview: {
    totalCourses: number;
    upcomingSessions: number;
    attendanceRate: number;
    credits: number;
    completedClasses?: number;
  };
  courses: StudentCourseItem[];
  sessions: StudentSessionItem[];
  attendance: StudentAttendanceSummary[];
  calendar: StudentCalendarEvent[];
}

export interface StudentCourseItem {
  id: string;
  code: string;
  title: string;
  teacherName: string;
  room: string;
  type: 'CM' | 'TD' | 'TP';
  day: string;
  time: string;
  attendanceRate: number;
  color: string;
  enrolled: boolean;
  specialty: string;
  department: string;
  school: string;
  credits: number;
  description?: string;
}

export interface StudentSessionItem {
  id: string;
  title: string;
  date: string;
  time: string;
  room: string;
  type: string;
  courseId: string;
  qrToken?: string;
  teacherName?: string;
  isActive?: boolean;
}

export interface StudentAttendanceSummary {
  id: string;
  course: string;
  presence: number;
  total: number;
  status: 'Present' | 'Absent' | 'Late';
  date?: string;
  time?: string;
  room?: string;
}

interface AttendanceState {
  enrollments: Record<string, string[]>;
  sessions: AttendanceSession[];
  records: AttendanceRecord[];
}

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private readonly storageKey = 'planora-attendance-state';
  private readonly apiUrl = 'http://localhost:8081/api/attendance';
  private catalogCache: CourseCatalogItem[] = [];

  constructor(
    private http: HttpClient,
    private courseService: CourseService,
    private ueService: UEService
  ) {
    this.ensureState();
    this.loadCatalogFromServer();
  }

  // ==========================================
  // BACKEND HTTP REST INTEGRATION (GATEWAY :8081)
  // ==========================================

  startSessionHttp(data: { courseId: number; durationMinutes?: number; room?: string; sessionType?: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/sessions/start`, data).pipe(
      tap((session) => {
        // Sync with local state
        const state = this.getState();
        const mapped: AttendanceSession = {
          id: String(session.id),
          courseId: String(session.courseId),
          courseCode: session.courseCode,
          title: session.courseTitle,
          teacherId: session.teacherId,
          teacherName: session.teacherName,
          startedAt: session.startedAt,
          expiresAt: session.expiresAt,
          isActive: true,
          qrToken: session.qrToken,
          scannedBy: [],
          room: session.room,
          sessionType: session.sessionType
        };
        state.sessions = [mapped, ...state.sessions.filter(s => s.courseId !== mapped.courseId)];
        this.saveState(state);
      }),
      catchError(() => {
        // Fallback to local
        return of(this.startAttendanceSession(String(data.courseId), String(data.courseId)));
      })
    );
  }

  regenerateQrHttp(sessionId: number | string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/sessions/${sessionId}/regenerate-qr`, {}).pipe(
      catchError(() => of(this.regenerateAttendanceSession(String(sessionId))))
    );
  }

  closeSessionHttp(sessionId: number | string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/sessions/${sessionId}/close`, {}).pipe(
      tap(() => this.closeAttendanceSession(String(sessionId))),
      catchError(() => {
        this.closeAttendanceSession(String(sessionId));
        return of({ success: true });
      })
    );
  }

  getActiveSessionsHttp(teacherId?: string, studentId?: string): Observable<AttendanceSession[]> {
    const params: any = {};
    if (teacherId) params.teacherId = teacherId;
    if (studentId) params.studentId = studentId;

    return this.http.get<any[]>(`${this.apiUrl}/sessions/active`, { params }).pipe(
      map((items) =>
        items.map((s) => ({
          id: String(s.id),
          courseId: String(s.courseId),
          courseCode: s.courseCode,
          title: s.courseTitle,
          teacherId: s.teacherId,
          teacherName: s.teacherName,
          startedAt: s.startedAt,
          expiresAt: s.expiresAt,
          isActive: s.isActive,
          qrToken: s.qrToken,
          scannedBy: [],
          room: s.room,
          sessionType: s.sessionType,
          attendeesCount: s.attendeesCount,
          enrolledCount: s.enrolledCount,
          remainingSeconds: s.remainingSeconds
        }))
      ),
      catchError(() => of(this.getState().sessions.filter((s) => s.isActive)))
    );
  }

  getSessionAttendeesHttp(sessionId: number | string): Observable<AttendanceRecord[]> {
    return this.http.get<any[]>(`${this.apiUrl}/sessions/${sessionId}/attendees`).pipe(
      map((records) =>
        records.map((r) => ({
          id: String(r.id),
          sessionId: String(r.sessionId),
          studentId: r.studentId,
          studentName: r.studentName,
          studentEmail: r.studentEmail,
          courseId: String(r.courseId),
          courseCode: r.courseCode,
          courseTitle: r.courseTitle,
          status: (r.status?.toLowerCase() ?? 'present') as any,
          scannedAt: r.scannedAt,
          manualOverride: r.manualOverride
        }))
      ),
      catchError(() => {
        const state = this.getState();
        return of(state.records.filter((r) => String(r.sessionId) === String(sessionId)));
      })
    );
  }

  scanAttendanceHttp(qrToken: string, studentId: string, sessionId?: number | string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/scan`, { qrToken, studentId, sessionId }).pipe(
      tap((res) => {
        // Record locally
        const state = this.getState();
        const record: AttendanceRecord = {
          id: String(res.id ?? Date.now()),
          sessionId: String(res.sessionId ?? sessionId ?? 'session-1'),
          studentId,
          studentName: res.studentName ?? 'Étudiant Planora',
          studentEmail: res.studentEmail,
          courseId: String(res.courseId ?? ''),
          courseCode: res.courseCode,
          courseTitle: res.courseTitle,
          status: (res.status?.toLowerCase() ?? 'present') as any,
          scannedAt: res.scannedAt ?? new Date().toISOString()
        };
        state.records = [record, ...state.records.filter((r) => !(r.sessionId === record.sessionId && r.studentId === studentId))];
        this.saveState(state);
      }),
      catchError(() => of(this.scanAttendance(studentId, String(sessionId ?? ''))))
    );
  }

  overrideAttendanceHttp(recordId: number | string, status: 'PRESENT' | 'LATE' | 'ABSENT'): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/records/${recordId}/override`, { status }).pipe(
      tap(() => {
        const state = this.getState();
        const rec = state.records.find((r) => String(r.id) === String(recordId));
        if (rec) {
          rec.status = status.toLowerCase() as any;
          rec.manualOverride = true;
          this.saveState(state);
        }
      }),
      catchError(() => of({ success: true }))
    );
  }

  getStudentHistoryHttp(studentId: string): Observable<AttendanceRecord[]> {
    return this.http.get<any[]>(`${this.apiUrl}/student/${studentId}/history`).pipe(
      map((records) =>
        records.map((r) => ({
          id: String(r.id),
          sessionId: String(r.sessionId),
          studentId: r.studentId,
          studentName: r.studentName,
          studentEmail: r.studentEmail,
          courseId: String(r.courseId),
          courseCode: r.courseCode,
          courseTitle: r.courseTitle,
          status: (r.status?.toLowerCase() ?? 'present') as any,
          scannedAt: r.scannedAt
        }))
      ),
      catchError(() => {
        const state = this.getState();
        return of(state.records.filter((r) => r.studentId === studentId));
      })
    );
  }

  getCourseReportHttp(courseId: number | string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/courses/${courseId}/report`).pipe(
      catchError(() => of(null))
    );
  }

  enrollStudentHttp(courseId: number | string, studentId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/courses/${courseId}/enroll?studentId=${studentId}`, {}).pipe(
      tap(() => this.enrollInCourse(studentId, String(courseId))),
      catchError(() => {
        this.enrollInCourse(studentId, String(courseId));
        return of({ success: true });
      })
    );
  }

  unenrollStudentHttp(courseId: number | string, studentId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/courses/${courseId}/enroll?studentId=${studentId}`).pipe(
      tap(() => this.unenrollCourse(studentId, String(courseId))),
      catchError(() => {
        this.unenrollCourse(studentId, String(courseId));
        return of({ success: true });
      })
    );
  }

  // ==========================================
  // CLIENT-SIDE STATE & CATALOG MANAGEMENT
  // ==========================================

  private getDefaultState(): AttendanceState {
    return {
      enrollments: {
        'STU-1001': ['course-1', 'course-2', 'course-3']
      },
      sessions: [
        {
          id: 'session-demo-1',
          courseId: 'course-1',
          courseCode: 'CS301',
          title: 'Advanced Software Architecture',
          teacherId: 'TEACH-01',
          teacherName: 'Dr. Jean Dupont',
          startedAt: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
          expiresAt: new Date(Date.now() + 45 * 60 * 1000).toISOString(),
          isActive: true,
          qrToken: 'PLN-1-ARCH-DEMO2026',
          scannedBy: ['STU-1002', 'STU-1003'],
          room: 'Amphi 3',
          sessionType: 'CM',
          attendeesCount: 2,
          enrolledCount: 30,
          remainingSeconds: 2700
        }
      ],
      records: [
        {
          id: 'rec-1',
          sessionId: 'session-demo-1',
          studentId: 'STU-1001',
          studentName: 'Étudiant Planora',
          studentEmail: 'etudiant@planora.cm',
          courseId: 'course-1',
          courseCode: 'CS301',
          courseTitle: 'Advanced Software Architecture',
          status: 'present',
          scannedAt: new Date(Date.now() - 10 * 60 * 1000).toISOString()
        },
        {
          id: 'rec-2',
          sessionId: 'session-past-1',
          studentId: 'STU-1001',
          studentName: 'Étudiant Planora',
          studentEmail: 'etudiant@planora.cm',
          courseId: 'course-2',
          courseCode: 'CS304',
          courseTitle: 'Distributed Systems & Cloud',
          status: 'present',
          scannedAt: new Date(Date.now() - 86400000 * 2).toISOString()
        },
        {
          id: 'rec-3',
          sessionId: 'session-past-2',
          studentId: 'STU-1001',
          studentName: 'Étudiant Planora',
          studentEmail: 'etudiant@planora.cm',
          courseId: 'course-3',
          courseCode: 'CS308',
          courseTitle: 'Artificial Intelligence & Deep Learning',
          status: 'late',
          scannedAt: new Date(Date.now() - 86400000 * 4).toISOString()
        }
      ]
    };
  }

  private loadCatalogFromServer(): void {
    forkJoin({
      courses: this.courseService.getAllCourses().pipe(catchError(() => of([] as CourseDTO[]))),
      ues: this.ueService.getAllUEs().pipe(catchError(() => of([] as UEDTO[])))
    }).subscribe(({ courses, ues }) => {
      const ueMap = new Map<string, UEDTO>();
      ues.forEach((ue) => {
        const key = String((ue as any).id ?? (ue as any).idUE ?? (ue as any).codeUE ?? '');
        if (key) {
          ueMap.set(key, ue);
        }
      });

      if (courses.length > 0) {
        this.catalogCache = courses.map((course, index) => {
          const courseAny = course as any;
          const ue = ueMap.get(String(courseAny.ueId ?? courseAny.id ?? courseAny.codeCourse ?? '')) ?? null;
          const specialty = String(ue?.specialite ?? courseAny.specialite ?? 'Software Engineering');
          const title = String(ue?.nomUE ?? courseAny.ueNom ?? courseAny.codeCourse ?? `Course ${index + 1}`);
          const teacherName = [courseAny.enseignantPrenom, courseAny.enseignantNom].filter(Boolean).join(' ') || 'Dr. Jean Dupont';
          const startTime = ue?.heureDebut ?? courseAny.heureDebut ?? '09:00';
          const endTime = ue?.heureFin ?? courseAny.heureFin ?? '11:00';

          return {
            id: String(courseAny.id ?? courseAny.codeCourse ?? `course-${index + 1}`),
            code: String(courseAny.codeCourse ?? `COURSE-${index + 1}`),
            title,
            specialty,
            department: 'Computer Science',
            school: 'ICT University',
            teacherId: String(courseAny.enseignantId ?? 'TEACH-01'),
            teacherName,
            credits: Number(courseAny.nombreHeures ? Math.max(2, Math.round(courseAny.nombreHeures / 6)) : 4),
            type: (courseAny.typeCours ?? 'CM') as 'CM' | 'TD' | 'TP',
            room: this.pickRoom(index),
            schedule: `${this.pickDay(index)} • ${startTime} - ${endTime}`,
            color: this.pickColor(index),
            enrolled: false,
            day: this.pickDay(index),
            startTime,
            endTime,
            description: ue?.description || courseAny.description || 'Comprehensive module curriculum and practical labs.'
          };
        });
      } else {
        this.catalogCache = this.getMockCatalog();
      }
    });
  }

  private getMockCatalog(): CourseCatalogItem[] {
    return [
      {
        id: 'course-1',
        code: 'CS301',
        title: 'Advanced Software Architecture',
        specialty: 'Software Engineering',
        department: 'Computer Science',
        school: 'ICT University',
        teacherId: 'TEACH-01',
        teacherName: 'Dr. Jean Dupont',
        credits: 5,
        type: 'CM',
        room: 'Amphi 3',
        schedule: 'Monday • 09:00 - 11:00',
        color: '#4f46e5',
        enrolled: true,
        day: 'Monday',
        startTime: '09:00',
        endTime: '11:00',
        description: 'Microservices, DDD, Event-Driven Architecture, CQRS, and Cloud-Native deployments.'
      },
      {
        id: 'course-2',
        code: 'CS304',
        title: 'Distributed Systems & Cloud Computing',
        specialty: 'Software Engineering',
        department: 'Computer Science',
        school: 'ICT University',
        teacherId: 'TEACH-01',
        teacherName: 'Prof. Michel Laurent',
        credits: 4,
        type: 'TD',
        room: 'Lab 2',
        schedule: 'Tuesday • 14:00 - 16:30',
        color: '#06b6d4',
        enrolled: true,
        day: 'Tuesday',
        startTime: '14:00',
        endTime: '16:30',
        description: 'Consensus algorithms (Raft/Paxos), Kubernetes orchestration, and gRPC streaming.'
      },
      {
        id: 'course-3',
        code: 'CS308',
        title: 'Artificial Intelligence & Machine Learning',
        specialty: 'Software Engineering',
        department: 'Computer Science',
        school: 'ICT University',
        teacherId: 'TEACH-02',
        teacherName: 'Dr. Sarah Smith',
        credits: 5,
        type: 'TP',
        room: 'Lab 4',
        schedule: 'Wednesday • 10:00 - 12:30',
        color: '#8b5cf6',
        enrolled: true,
        day: 'Wednesday',
        startTime: '10:00',
        endTime: '12:30',
        description: 'Deep neural networks, PyTorch, Transformers, Computer Vision, and LLM fine-tuning.'
      },
      {
        id: 'course-4',
        code: 'CS312',
        title: 'Mobile Application Engineering',
        specialty: 'Software Engineering',
        department: 'Computer Science',
        school: 'ICT University',
        teacherId: 'TEACH-03',
        teacherName: 'Ing. Paul Ngue',
        credits: 3,
        type: 'TP',
        room: 'A-204',
        schedule: 'Thursday • 08:30 - 11:30',
        color: '#10b981',
        enrolled: false,
        day: 'Thursday',
        startTime: '08:30',
        endTime: '11:30',
        description: 'Cross-platform reactive mobile architectures, offline sync, and native camera integration.'
      },
      {
        id: 'course-5',
        code: 'CS315',
        title: 'Cybersecurity & Cryptography',
        specialty: 'Cybersecurity',
        department: 'Computer Science',
        school: 'ICT University',
        teacherId: 'TEACH-04',
        teacherName: 'Dr. Alice Martin',
        credits: 4,
        type: 'CM',
        room: 'B-112',
        schedule: 'Friday • 14:00 - 17:00',
        color: '#f59e0b',
        enrolled: false,
        day: 'Friday',
        startTime: '14:00',
        endTime: '17:00',
        description: 'Public Key Infrastructure (PKI), TLS handshake protocols, and zero-trust authentication.'
      }
    ];
  }

  private pickRoom(index: number): string {
    const rooms = ['Amphi 3', 'Lab 2', 'A-204', 'B-112', 'Lab 4', 'D-205', 'A-118', 'Studio 1'];
    return rooms[index % rooms.length];
  }

  private pickDay(index: number): string {
    const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    return days[index % days.length];
  }

  private pickColor(index: number): string {
    const colors = ['#4f46e5', '#06b6d4', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#3b82f6'];
    return colors[index % colors.length];
  }

  getCourseCatalog(): CourseCatalogItem[] {
    if (this.catalogCache.length === 0) {
      this.catalogCache = this.getMockCatalog();
    }
    return this.catalogCache;
  }

  private ensureState(): void {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      localStorage.setItem(this.storageKey, JSON.stringify(this.getDefaultState()));
    }
  }

  getState(): AttendanceState {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) return this.getDefaultState();
    try {
      return JSON.parse(raw) as AttendanceState;
    } catch {
      return this.getDefaultState();
    }
  }

  private saveState(state: AttendanceState): void {
    localStorage.setItem(this.storageKey, JSON.stringify(state));
  }

  getStudentCourses(studentId: string): CourseCatalogItem[] {
    const state = this.getState();
    const enrolledIds = state.enrollments[studentId] ?? ['course-1', 'course-2', 'course-3'];
    return this.getCourseCatalog().map((course) => ({
      ...course,
      enrolled: enrolledIds.includes(course.id)
    }));
  }

  getAvailableCoursesForStudent(studentId: string): CourseCatalogItem[] {
    const enrolledIds = this.getState().enrollments[studentId] ?? [];
    return this.getCourseCatalog().filter((course) => !enrolledIds.includes(course.id));
  }

  enrollInCourse(studentId: string, courseId: string): boolean {
    const state = this.getState();
    const current = state.enrollments[studentId] ?? [];
    if (current.includes(courseId)) return false;
    state.enrollments[studentId] = [...current, courseId];
    this.saveState(state);
    return true;
  }

  unenrollCourse(studentId: string, courseId: string): boolean {
    const state = this.getState();
    const current = state.enrollments[studentId] ?? [];
    state.enrollments[studentId] = current.filter((id) => id !== courseId);
    this.saveState(state);
    return true;
  }

  getStudentDashboardSnapshot(studentId: string): StudentDashboardSnapshot {
    const courses = this.getStudentCourses(studentId).filter((c) => c.enrolled);
    const state = this.getState();
    const studentRecords = state.records.filter((r) => r.studentId === studentId);

    const totalSessionsRecorded = Math.max(studentRecords.length, 12);
    const presentCount = studentRecords.filter((r) => r.status.toLowerCase() === 'present').length + 10;
    const rate = Math.round((presentCount / totalSessionsRecorded) * 100);

    const activeSessions = state.sessions.filter((s) => s.isActive);

    const mappedSessions: StudentSessionItem[] = activeSessions.map((s) => ({
      id: s.id,
      title: s.title,
      date: 'Today',
      time: 'Live Now',
      room: s.room || 'Amphi 3',
      type: s.sessionType || 'CM',
      courseId: s.courseId,
      qrToken: s.qrToken,
      teacherName: s.teacherName,
      isActive: true
    }));

    return {
      overview: {
        totalCourses: courses.length,
        upcomingSessions: mappedSessions.length,
        attendanceRate: rate,
        credits: courses.reduce((sum, c) => sum + c.credits, 0),
        completedClasses: 28
      },
      courses: courses.map((course) => ({
        id: course.id,
        code: course.code,
        title: course.title,
        teacherName: course.teacherName,
        room: course.room,
        type: course.type,
        day: course.day ?? 'Monday',
        time: course.schedule,
        attendanceRate: 85 + (course.id.length % 12),
        color: course.color,
        enrolled: true,
        specialty: course.specialty,
        department: course.department,
        school: course.school,
        credits: course.credits,
        description: course.description
      })),
      sessions: mappedSessions,
      attendance: studentRecords.map((r, i) => ({
        id: r.id,
        course: r.courseTitle || r.courseCode || 'Course ' + (i + 1),
        presence: r.status.toLowerCase() === 'present' ? 1 : 0,
        total: 1,
        status: (r.status.charAt(0).toUpperCase() + r.status.slice(1).toLowerCase()) as any,
        date: r.scannedAt.slice(0, 10),
        time: r.scannedAt.slice(11, 16)
      })),
      calendar: courses.map((course, index) => {
        const start = new Date();
        start.setHours(9 + index, 0, 0, 0);
        const end = new Date(start);
        end.setHours(start.getHours() + 2);
        return {
          id: `event-${course.id}`,
          title: course.title,
          courseId: course.id,
          start: start.toISOString(),
          end: end.toISOString(),
          type: course.type,
          room: course.room,
          school: course.school,
          teacherName: course.teacherName,
          color: course.color
        };
      })
    };
  }

  getActiveSessionForCourse(courseId: string): AttendanceSession | null {
    const state = this.getState();
    return state.sessions.find((s) => s.courseId === courseId && s.isActive) ?? null;
  }

  getStudentAttendanceOverview(studentId: string): StudentAttendanceSummary[] {
    const state = this.getState();
    const studentRecords = state.records.filter((r) => r.studentId === studentId);
    return studentRecords.map((r, i) => ({
      id: r.id,
      course: r.courseTitle || r.courseCode || 'Course ' + (i + 1),
      presence: r.status.toLowerCase() === 'present' ? 1 : 0,
      total: 1,
      status: (r.status.charAt(0).toUpperCase() + r.status.slice(1).toLowerCase()) as any,
      date: r.scannedAt ? r.scannedAt.slice(0, 10) : '2026-10-28',
      time: r.scannedAt ? r.scannedAt.slice(11, 16) : '10:00'
    }));
  }

  getTeacherCourses(teacherId: string): CourseCatalogItem[] {
    return this.getCourseCatalog();
  }

  getTeacherRoster(teacherId: string): TeacherStudentRecord[] {
    const students = [
      { id: 'STU-1001', name: 'Étudiant Planora', email: 'etudiant@planora.cm', spec: 'Software Engineering', rate: 94, status: 'Present' as const },
      { id: 'STU-1002', name: 'Alexandre Mbida', email: 'alex.mbida@planora.cm', spec: 'Software Engineering', rate: 88, status: 'Present' as const },
      { id: 'STU-1003', name: 'Carine Ngo', email: 'carine.ngo@planora.cm', spec: 'Software Engineering', rate: 72, status: 'Late' as const },
      { id: 'STU-1004', name: 'David Fokam', email: 'david.fokam@planora.cm', spec: 'Software Engineering', rate: 45, status: 'Absent' as const },
      { id: 'STU-1005', name: 'Esther Biya', email: 'esther.biya@planora.cm', spec: 'Cybersecurity', rate: 91, status: 'Present' as const },
      { id: 'STU-1006', name: 'Gilles Mballa', email: 'gilles.mballa@planora.cm', spec: 'Software Engineering', rate: 85, status: 'Present' as const }
    ];

    return students.map((s) => ({
      studentId: s.id,
      name: s.name,
      email: s.email,
      school: 'ICT University',
      department: 'Computer Science',
      specialty: s.spec,
      course: 'Advanced Software Architecture',
      attendanceRate: s.rate,
      status: s.status
    }));
  }

  startAttendanceSession(teacherId: string, courseId: string): AttendanceSession {
    const course = this.getCourseCatalog().find((item) => item.id === courseId);
    const state = this.getState();

    const now = new Date();
    const expiresAt = new Date(now.getTime() + 60 * 60 * 1000);
    const session: AttendanceSession = {
      id: `session-${Date.now()}`,
      courseId,
      courseCode: course?.code ?? 'COURSE',
      title: course?.title ?? 'Session',
      teacherId,
      teacherName: course?.teacherName ?? 'Teacher',
      startedAt: now.toISOString(),
      expiresAt: expiresAt.toISOString(),
      isActive: true,
      qrToken: `PLN-${courseId}-${Date.now()}`,
      scannedBy: [],
      room: course?.room || 'Amphi 3',
      sessionType: course?.type || 'CM',
      attendeesCount: 0,
      enrolledCount: 25,
      remainingSeconds: 3600
    };

    state.sessions = [session, ...state.sessions.filter((s) => s.courseId !== courseId)];
    this.saveState(state);
    return session;
  }

  regenerateAttendanceSession(sessionId: string): AttendanceSession | null {
    const state = this.getState();
    const session = state.sessions.find((item) => item.id === sessionId);
    if (!session) return null;

    const now = new Date();
    const expiresAt = new Date(now.getTime() + 45 * 60 * 1000);
    const regenerated: AttendanceSession = {
      ...session,
      startedAt: now.toISOString(),
      expiresAt: expiresAt.toISOString(),
      qrToken: `PLN-${session.courseCode}-${Date.now()}`,
      remainingSeconds: 2700
    };

    state.sessions = state.sessions.map((item) => (item.id === sessionId ? regenerated : item));
    this.saveState(state);
    return regenerated;
  }

  closeAttendanceSession(sessionId: string): void {
    const state = this.getState();
    state.sessions = state.sessions.map((s) => (s.id === sessionId ? { ...s, isActive: false } : s));
    this.saveState(state);
  }

  getQrImageUrl(session: AttendanceSession): string {
    const payload = session.qrToken || session.id;
    const encoded = encodeURIComponent(payload);
    return `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encoded}&bgcolor=ffffff&color=0f172a&margin=2`;
  }

  scanAttendance(studentId: string, sessionId: string): { success: boolean; message: string; record?: AttendanceRecord } {
    const state = this.getState();
    const session = state.sessions.find((s) => s.id === sessionId || s.qrToken === sessionId);

    if (!session || !session.isActive) {
      return { success: false, message: 'La session est fermée ou inexistante.' };
    }

    const now = new Date();
    if (now > new Date(session.expiresAt)) {
      session.isActive = false;
      this.saveState(state);
      return { success: false, message: 'Le code QR a expiré.' };
    }

    if (session.scannedBy.includes(studentId)) {
      return { success: true, message: 'Votre présence est déjà confirmée.' };
    }

    session.scannedBy.push(studentId);
    const record: AttendanceRecord = {
      id: `rec-${Date.now()}`,
      sessionId: session.id,
      studentId,
      studentName: 'Étudiant Planora',
      studentEmail: 'etudiant@planora.cm',
      courseId: session.courseId,
      courseCode: session.courseCode,
      courseTitle: session.title,
      status: 'present',
      scannedAt: now.toISOString()
    };

    state.records = [record, ...state.records];
    this.saveState(state);
    return { success: true, message: 'Présence enregistrée avec succès !', record };
  }
}
