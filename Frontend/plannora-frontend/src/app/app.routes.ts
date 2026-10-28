import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { EnseignantDashboardComponent } from './enseignant-dashboard/enseignant-dashboard.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { StudentDashboardComponent } from './student-dashboard/student-dashboard.component';
import { TeacherCoursesComponent } from './teacher-courses/teacher-courses.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { authGuard } from './guards/auth.guard';
import { StudentCourseEnrollmentComponent } from './student-course-enrollment/student-course-enrollment.component';
import { StudentAttendanceQrComponent } from './student-dashboard/student-attendance-qr.component';
import { TeacherAttendanceComponent } from './teacher-attendance/teacher-attendance.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'enseignant-dashboard',
    component: EnseignantDashboardComponent,
    canActivate: [authGuard],
    data: { role: 'ENSEIGNANT' }
  },
  {
    path: 'student-dashboard',
    component: StudentDashboardComponent,
    canActivate: [authGuard],
    data: { role: 'ETUDIANT' }
  },
  {
    path: 'student-course-enrollment',
    component: StudentCourseEnrollmentComponent,
    canActivate: [authGuard],
    data: { role: 'ETUDIANT' }
  },
  {
    path: 'student-attendance-qr',
    component: StudentAttendanceQrComponent,
    canActivate: [authGuard],
    data: { role: 'ETUDIANT' }
  },
  {
    path: 'teacher-attendance',
    component: TeacherAttendanceComponent,
    canActivate: [authGuard],
    data: { role: 'ENSEIGNANT' }
  },
  {
    path: 'teacher-courses',
    component: TeacherCoursesComponent,
    canActivate: [authGuard],
    data: { role: 'ENSEIGNANT' }
  },
  {
    path: 'admin-dashboard',
    component: AdminDashboardComponent,
    canActivate: [authGuard],
    data: { role: 'ADMIN' }
  },
  { path: 'reset-password', component: ResetPasswordComponent }
];
