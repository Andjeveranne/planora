# Comprehensive Prompt for Antigravity: QR Attendance Management System (Student & Teacher Dashboards)

Please design, code, and structure a fully-featured, modern, and data-dense **QR Code Attendance Management System** incorporating both the **Student Dashboard** and **Teacher Dashboard**. The design must be professional, responsive, feature-rich, and polished with modern UI components (utilizing clean cards, data tables with search/filter/pagination, interactive modals, and real-time visual elements).

---

## 1. STUDENT DASHBOARD SPECIFICATIONS

### A. Index / Overview Page (Data-Dense & Rich Statistics)
* **Statistics Summary Cards:** Display key metrics at the top (Total Enrolled Courses, Active Ongoing Sessions Today, Total Attendance Rate %, Completed Classes).
* **Quick Access & Recent Data:** Quick links to active sessions, recently enrolled courses, and recent attendance logs.
* **Interactive Modals:** Use clean modal popups for course enrollment confirmations, viewing quick stats, and scanning QR codes.

### B. Course Section
* **Specialty Courses Catalog:** View courses specific to the student's specialty and year/level.
* **Course Details & Enrollment:** Click a course card to open a modal or dedicated view showing full course details (syllabus, teacher info, schedule). Include an active **"Enroll in Course"** button with instant feedback.

### C. Live Sessions & QR Code Attendance Scanning
* **Active Sessions Section:** Displays currently ongoing course sessions in real-time.
* **Mark Attendance / QR Scanner:** 
  * Students can click into an ongoing session.
  * Provide a built-in camera/file-upload QR code scanner interface (or simulated scan input) where the student scans the dynamic QR code displayed on the teacher's dashboard.
  * Instant validation workflow: once scanned and validated, the student is marked present automatically.
* **Live Attendee List:** View a live-updated table of other classmates who have already checked in and are marked present for that session.

### D. Attendance History Section
* **Comprehensive Records:** A complete historical log of all attended and missed sessions.
* **Advanced Data Tables:** Must include built-in **search bars**, **status filters** (Present, Absent, Late), and **pagination**.

---

## 2. TEACHER DASHBOARD SPECIFICATIONS

### A. Courses & Session Management
* **Course Catalog:** View all assigned courses. Clicking a course reveals full course details, enrolled students list, and session history.
* **Automated & Manual Session Control:**
  * Sessions should automatically trigger when the scheduled course time arrives, displaying a live **countdown timer**.
  * Teachers can also manually start a session with a single click.
* **Dynamic QR Code Generation:** Once a session starts, the system generates a secure, dynamic QR code on the screen that refreshes or locks for student check-ins.

### B. Live Ongoing Sessions Section
* **Active Monitoring:** Dedicated section for currently running sessions.
* **Live Attendance Tracking Table:** 
  * Real-time list and counter of students who have successfully scanned and checked in.
  * Table must feature search, filtering, and pagination to manage large classes easily.
  * Manual override option (allow teachers to manually mark a student present or absent if needed).

### C. Attendance Reports & History
* **Per-Course Analytics:** View historical attendance records, download reports, and track student attendance percentages across semesters.

---

## 3. UI/UX & TECHNICAL REQUIREMENTS
* **Design System:** Modern dashboard aesthetic (clean typography, subtle shadows, badge indicators for active/inactive sessions, status colors for attendance).
* **Interactive Elements:** Ensure all tables support **search, column filtering, and pagination**. Use **modals** for quick actions (enrollment, viewing details, scanning prompts) to avoid unnecessary page reloads.
* **Responsiveness:** Fully responsive across desktop, tablet, and mobile devices (especially critical for students scanning QR codes via mobile).