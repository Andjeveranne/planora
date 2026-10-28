import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import {
  AttendanceRecord,
  AttendanceService,
  AttendanceSession,
  CourseCatalogItem,
  StudentDashboardSnapshot
} from '../services/attendance.service';
import { StudentProfile, StudentService } from '../services/student.service';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './student-dashboard.component.html',
  styleUrls: ['./student-dashboard.component.css']
})
export class StudentDashboardComponent implements OnInit, OnDestroy {
  activeSection: 'overview' | 'courses' | 'sessions' | 'attendance' = 'overview';
  dashboardData: StudentDashboardSnapshot | null = null;
  activeSessions: AttendanceSession[] = [];
  selectedSession: AttendanceSession | null = null;
  sessionAttendees: AttendanceRecord[] = [];

  // Student Profile
  student: StudentProfile = {
    id: 'STU-1001',
    nom: 'Planora',
    prenom: 'Alexandre',
    email: 'alex.planora@ictuniversity.edu',
    school: 'ICT University',
    department: 'Computer Science',
    specialty: 'Software Engineering',
    level: 'L3',
    status: 'Actif'
  };

  // Course Catalog & Enrollment
  courseCatalog: CourseCatalogItem[] = [];
  courseSearchTerm = '';
  selectedSpecialty = 'all';
  selectedType = 'all';
  selectedLevel = 'all';
  selectedCourseForModal: CourseCatalogItem | null = null;
  isCourseModalOpen = false;
  enrollmentFeedback = '';

  // Quick Stats Breakdown Modal
  quickStatsModalType: 'courses' | 'attendance' | 'sessions' | 'classes' | null = null;

  // QR Scanner Modal State
  isScannerOpen = false;
  scanInputToken = '';
  scanStatus: 'idle' | 'validating' | 'success' | 'error' = 'idle';
  scanFeedback = '';
  lastScannedRecord: AttendanceRecord | null = null;
  isCameraActive = false;

  // Attendance History Advanced Table
  historySearch = '';
  historyFilter: 'all' | 'present' | 'late' | 'absent' = 'all';
  historyPage = 1;
  historyPageSize = 5;

  private pollSub?: Subscription;

  constructor(
    private attendanceService: AttendanceService,
    private studentService: StudentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.refreshAll();

    // Auto poll active sessions every 10 seconds for real-time live updates
    this.pollSub = interval(10000).subscribe(() => {
      this.loadActiveSessions();
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
    this.stopCameraStream();
  }

  setSection(section: 'overview' | 'courses' | 'sessions' | 'attendance'): void {
    this.activeSection = section;
  }

  loadProfile(): void {
    this.studentService.getStudentsFromApi().subscribe({
      next: (students) => {
        const found = students.find((s) => s.id === this.student.id) || students[0];
        if (found) {
          this.student = { ...this.student, ...found };
        }
      },
      error: () => {}
    });
  }

  refreshAll(): void {
    this.dashboardData = this.attendanceService.getStudentDashboardSnapshot(this.student.id);
    this.courseCatalog = this.attendanceService.getStudentCourses(this.student.id);
    this.loadActiveSessions();
  }

  loadActiveSessions(): void {
    this.attendanceService.getActiveSessionsHttp('', this.student.id).subscribe({
      next: (sessions) => {
        this.activeSessions = sessions;
        if (this.selectedSession) {
          const updated = sessions.find((s) => s.id === this.selectedSession!.id);
          if (updated) {
            this.selectedSession = updated;
            this.loadAttendees(updated.id);
          }
        }
      },
      error: () => {
        this.activeSessions = this.attendanceService.getState().sessions.filter((s) => s.isActive);
      }
    });
  }

  openStatsModal(type: 'courses' | 'attendance' | 'sessions' | 'classes'): void {
    this.quickStatsModalType = type;
  }

  closeStatsModal(): void {
    this.quickStatsModalType = null;
  }

  loadAttendees(sessionId: string): void {
    this.attendanceService.getSessionAttendeesHttp(sessionId).subscribe({
      next: (records) => {
        this.sessionAttendees = records;
      },
      error: () => {
        this.sessionAttendees = this.attendanceService.getState().records.filter((r) => r.sessionId === sessionId);
      }
    });
  }

  // ==========================================
  // COURSE CATALOG & ENROLLMENT
  // ==========================================

  get specialtiesList(): string[] {
    const set = new Set(this.courseCatalog.map((c) => c.specialty).filter(Boolean));
    return Array.from(set);
  }

  get filteredCourses(): CourseCatalogItem[] {
    const term = this.courseSearchTerm.trim().toLowerCase();
    return this.courseCatalog.filter((course) => {
      const matchSearch =
        !term ||
        course.title.toLowerCase().includes(term) ||
        course.code.toLowerCase().includes(term) ||
        course.teacherName.toLowerCase().includes(term);
      const matchSpec = this.selectedSpecialty === 'all' || course.specialty === this.selectedSpecialty;
      const matchType = this.selectedType === 'all' || course.type === this.selectedType;
      return matchSearch && matchSpec && matchType;
    });
  }

  openCourseDetails(course: CourseCatalogItem): void {
    this.selectedCourseForModal = course;
    this.enrollmentFeedback = '';
    this.isCourseModalOpen = true;
  }

  closeCourseModal(): void {
    this.isCourseModalOpen = false;
    this.selectedCourseForModal = null;
  }

  toggleEnrollment(course: CourseCatalogItem): void {
    if (course.enrolled) {
      this.attendanceService.unenrollStudentHttp(course.id, this.student.id).subscribe(() => {
        course.enrolled = false;
        this.enrollmentFeedback = 'Course dropped successfully.';
        this.refreshAll();
      });
    } else {
      this.attendanceService.enrollStudentHttp(course.id, this.student.id).subscribe(() => {
        course.enrolled = true;
        this.enrollmentFeedback = 'Course enrollment confirmed successfully!';
        this.refreshAll();
      });
    }
  }

  // ==========================================
  // LIVE SESSIONS & QR SCANNER
  // ==========================================

  openSessionView(session: AttendanceSession): void {
    this.selectedSession = session;
    this.loadAttendees(session.id);
  }

  openScannerForSession(session: AttendanceSession): void {
    this.selectedSession = session;
    this.scanInputToken = session.qrToken;
    this.scanStatus = 'idle';
    this.scanFeedback = '';
    this.lastScannedRecord = null;
    this.isScannerOpen = true;
    this.startCamera();
  }

  openGeneralScanner(): void {
    this.selectedSession = this.activeSessions.length > 0 ? this.activeSessions[0] : null;
    this.scanInputToken = this.selectedSession?.qrToken || '';
    this.scanStatus = 'idle';
    this.scanFeedback = '';
    this.lastScannedRecord = null;
    this.isScannerOpen = true;
    this.startCamera();
  }

  closeScanner(): void {
    this.isScannerOpen = false;
    this.stopCameraStream();
  }

  startCamera(): void {
    this.isCameraActive = true;
    setTimeout(() => {
      const video = document.getElementById('scanner-video') as HTMLVideoElement;
      if (video && navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        navigator.mediaDevices
          .getUserMedia({ video: { facingMode: 'environment' } })
          .then((stream) => {
            video.srcObject = stream;
            video.play();
          })
          .catch(() => {
            this.isCameraActive = false;
          });
      } else {
        this.isCameraActive = false;
      }
    }, 200);
  }

  stopCameraStream(): void {
    this.isCameraActive = false;
    const video = document.getElementById('scanner-video') as HTMLVideoElement;
    if (video && video.srcObject) {
      const stream = video.srcObject as MediaStream;
      stream.getTracks().forEach((track) => track.stop());
      video.srcObject = null;
    }
  }

  submitScan(): void {
    if (!this.scanInputToken.trim()) {
      this.scanStatus = 'error';
      this.scanFeedback = 'Please enter or scan a valid QR code token.';
      return;
    }

    this.scanStatus = 'validating';
    this.scanFeedback = 'Validating digital cryptographic attendance token...';

    this.attendanceService
      .scanAttendanceHttp(this.scanInputToken.trim(), this.student.id, this.selectedSession?.id)
      .subscribe({
        next: (record) => {
          this.scanStatus = 'success';
          this.scanFeedback = 'Attendance validated successfully! You are marked present.';
          this.lastScannedRecord = record;
          this.refreshAll();
          if (this.selectedSession) {
            this.loadAttendees(this.selectedSession.id);
          }
          this.stopCameraStream();
        },
        error: (err) => {
          this.scanStatus = 'error';
          this.scanFeedback = err.error?.message || 'Error validating QR code. Please check with your lecturer.';
        }
      });
  }

  onFileUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      // Simulate decoding QR from image file
      const token = this.selectedSession?.qrToken || 'PLN-SCAN-FILE-' + Date.now();
      this.scanInputToken = token;
      this.submitScan();
    }
  }

  // ==========================================
  // ATTENDANCE HISTORY & ADVANCED TABLE
  // ==========================================

  get filteredHistory(): any[] {
    if (!this.dashboardData) return [];
    let list = this.dashboardData.attendance;

    if (this.historyFilter !== 'all') {
      list = list.filter((item) => item.status.toLowerCase() === this.historyFilter.toLowerCase());
    }

    if (this.historySearch.trim()) {
      const term = this.historySearch.trim().toLowerCase();
      list = list.filter((item) => item.course.toLowerCase().includes(term));
    }

    return list;
  }

  get paginatedHistory(): any[] {
    const start = (this.historyPage - 1) * this.historyPageSize;
    return this.filteredHistory.slice(start, start + this.historyPageSize);
  }

  get totalHistoryPages(): number {
    return Math.max(1, Math.ceil(this.filteredHistory.length / this.historyPageSize));
  }

  changeHistoryPage(page: number): void {
    this.historyPage = Math.min(Math.max(1, page), this.totalHistoryPages);
  }

  exportHistoryCSV(): void {
    const rows = [
      ['Date', 'Time', 'Course', 'Status', 'Room'],
      ...this.filteredHistory.map((h) => [h.date || '2026-10-28', h.time || '10:00', h.course, h.status, 'Hall 3'])
    ];
    const csvContent = 'data:text/csv;charset=utf-8,' + rows.map((e) => e.join(',')).join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `planora_attendance_${this.student.id}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
