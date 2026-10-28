import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StudentProfile, StudentService } from '../../services/student.service';
import { EcoleDTO, EcoleService } from '../../services/ecole.service';
import { ClassesService, FiliereDTO, SpecialiteDTO } from '../../services/classes.service';

@Component({
  selector: 'app-gestion-etudiants',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-etudiants.component.html',
  styleUrls: ['./gestion-etudiants.component.css']
})
export class GestionEtudiantsComponent implements OnInit {
  students: StudentProfile[] = [];
  filteredStudents: StudentProfile[] = [];
  search = '';
  selectedSchool = 'all';
  selectedDepartment = 'all';
  selectedLevel = 'all';
  showForm = false;
  isEditing = false;
  editingId: string | null = null;
  passwordConfirm = '';
  ecoles: EcoleDTO[] = [];
  filieres: FiliereDTO[] = [];
  specialites: SpecialiteDTO[] = [];

  form: StudentProfile = this.emptyStudent();

  schoolOptions: string[] = [];
  departmentOptions: string[] = [];
  levelOptions = ['L1', 'L2', 'L3', 'L4', 'L5', 'Master1', 'Master2'];
  schoolNameById: Record<string, string> = {};
  schoolIdByName: Record<string, string> = {};
  departmentIdByName: Record<string, Record<string, string>> = {};

  get availableDepartments(): string[] {
    if (!this.form.school) {
      return [];
    }
    const schoolId = this.resolveSchoolId(this.form.school);
    return this.filieres
      .filter((filiere) => filiere.idEcole === schoolId)
      .map((filiere) => filiere.nom)
      .sort((a, b) => a.localeCompare(b));
  }

  get availableSpecialties(): string[] {
    if (!this.form.school || !this.form.department) {
      return [];
    }
    const schoolId = this.resolveSchoolId(this.form.school);
    const departmentId = this.getDepartmentIdByName(this.form.department, schoolId);
    return this.specialites
      .filter((specialite) => specialite.idFiliere === departmentId && (!specialite.idEcole || specialite.idEcole === schoolId))
      .map((specialite) => specialite.nom)
      .sort((a, b) => a.localeCompare(b));
  }

  constructor(
    private studentService: StudentService,
    private ecoleService: EcoleService,
    private classesService: ClassesService
  ) {}

  ngOnInit(): void {
    this.loadAcademicData();
    this.loadStudents();
  }

  private normalizeSchoolDisplay(value?: string): string {
    if (!value) {
      return '';
    }
    return this.schoolNameById[value] ?? value;
  }

  private resolveSchoolId(value?: string): string {
    if (!value) {
      return '';
    }
    return this.schoolIdByName[value] ?? value;
  }

  private getDepartmentIdByName(departmentName: string, schoolId: string): string {
    const departmentId = this.departmentIdByName[schoolId]?.[departmentName];
    if (departmentId) {
      return departmentId;
    }
    const match = this.filieres.find(
      (filiere) => filiere.idEcole === schoolId && filiere.nom === departmentName
    );
    return match?.idFiliere || '';
  }

  private loadAcademicData(): void {
    this.ecoleService.obtenirToutes().subscribe({
      next: (ecoles) => {
        this.ecoles = ecoles;
        this.schoolNameById = Object.fromEntries(
          ecoles.filter((ecole) => !!ecole.idEcole).map((ecole) => [ecole.idEcole!, ecole.nomEcole])
        );
        this.schoolIdByName = Object.fromEntries(
          ecoles.filter((ecole) => !!ecole.idEcole).map((ecole) => [ecole.nomEcole, ecole.idEcole!])
        );
        this.schoolOptions = [...new Set(ecoles.map((ecole) => ecole.nomEcole).filter(Boolean))].sort((a, b) => a.localeCompare(b));
        this.refreshDepartmentOptions();
      },
      error: () => {
        this.ecoles = [];
        this.schoolOptions = [];
        this.departmentOptions = [];
      }
    });

    this.classesService.getFilieres().subscribe({
      next: (filieres) => {
        this.filieres = filieres;
        this.departmentIdByName = {};
        for (const filiere of filieres) {
          const schoolId = filiere.idEcole || '';
          if (!schoolId) continue;
          this.departmentIdByName[schoolId] ??= {};
          if (filiere.nom) {
            this.departmentIdByName[schoolId][filiere.nom] = filiere.idFiliere || '';
          }
        }
        this.refreshDepartmentOptions();
      }
    });

    this.classesService.getSpecialites().subscribe({
      next: (specialites) => {
        this.specialites = specialites;
      }
    });
  }

  private refreshDepartmentOptions(): void {
    this.departmentOptions = [...new Set(this.filieres.map((filiere) => filiere.nom).filter(Boolean))].sort((a, b) => a.localeCompare(b));
  }

  private refreshSchoolSelectionState(): void {
    const currentSchool = this.form.school;
    if (!currentSchool) {
      return;
    }

    const matchingSchoolName = this.schoolOptions.find((name) => name === currentSchool || this.resolveSchoolId(name) === currentSchool);
    if (matchingSchoolName) {
      this.form.school = matchingSchoolName;
      return;
    }

    const mappedSchool = this.normalizeSchoolDisplay(currentSchool);
    if (mappedSchool) {
      this.form.school = mappedSchool;
    }
  }

  private emptyStudent(): StudentProfile {
    return {
      id: '',
      nom: '',
      prenom: '',
      email: '',
      password: '',
      school: '',
      department: '',
      specialty: '',
      level: 'L3',
      status: 'Actif'
    };
  }

  loadStudents(): void {
    this.studentService.getStudentsFromApi().subscribe({
      next: (students) => {
        this.students = students.map((student) => ({
          ...student,
          school: this.normalizeSchoolDisplay(student.school) || student.school || ''
        }));
        this.studentService.saveStudentDirectory(this.students);
        this.applyFilters();
      },
      error: () => {
        this.students = this.studentService.getStudentDirectory().map((student) => ({
          ...student,
          school: this.normalizeSchoolDisplay(student.school) || student.school || ''
        }));
        this.applyFilters();
      }
    });
  }

  applyFilters(): void {
    const term = this.search.trim().toLowerCase();
    this.filteredStudents = this.students.filter((student) => {
      const matchesTerm = !term ||
        `${student.prenom} ${student.nom}`.toLowerCase().includes(term) ||
        student.email.toLowerCase().includes(term) ||
        student.specialty.toLowerCase().includes(term);
      const matchesSchool = this.selectedSchool === 'all' || student.school === this.selectedSchool;
      const matchesDepartment = this.selectedDepartment === 'all' || student.department === this.selectedDepartment;
      const matchesLevel = this.selectedLevel === 'all' || student.level === this.selectedLevel;
      return matchesTerm && matchesSchool && matchesDepartment && matchesLevel;
    });
  }

  openCreateForm(): void {
    this.form = this.emptyStudent();
    this.refreshSchoolSelectionState();
    this.passwordConfirm = '';
    this.isEditing = false;
    this.editingId = null;
    this.showForm = true;
  }

  openEditForm(student: StudentProfile): void {
    this.form = { ...student, password: '' };
    this.refreshSchoolSelectionState();
    this.passwordConfirm = '';
    this.isEditing = true;
    this.editingId = student.id;
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.form = this.emptyStudent();
    this.passwordConfirm = '';
    this.isEditing = false;
    this.editingId = null;
  }

  onSchoolChange(): void {
    this.refreshSchoolSelectionState();
    const departments = this.availableDepartments;
    const hasCurrentDepartment = departments.includes(this.form.department);
    this.form.department = hasCurrentDepartment ? this.form.department : '';
    this.form.specialty = '';
  }

  onDepartmentChange(): void {
    const specialties = this.availableSpecialties;
    const hasCurrentSpecialty = specialties.includes(this.form.specialty);
    this.form.specialty = hasCurrentSpecialty ? this.form.specialty : '';
  }

  saveStudent(): void {
    if (!this.form.nom || !this.form.prenom || !this.form.email || !this.form.school || !this.form.department || !this.form.specialty) {
      return;
    }

    const email = (this.form.email || '').trim().toLowerCase();
    const passwordValue = this.form.password?.trim() ?? '';

    if (!this.isEditing && passwordValue.length < 6) {
      return;
    }

    if (!this.isEditing && passwordValue !== this.passwordConfirm.trim()) {
      return;
    }

    const normalised = {
      ...this.form,
      email,
      password: passwordValue || 'student123',
      school: this.resolveSchoolId(this.form.school) || this.form.school
    };

    if (this.isEditing && this.editingId) {
      this.studentService.updateStudent(this.editingId, normalised).subscribe({
        next: () => {
          this.loadStudents();
          this.closeForm();
        },
        error: () => {
          this.students = this.students.map((student) => student.id === this.editingId ? { ...normalised, id: this.editingId, school: this.normalizeSchoolDisplay(normalised.school) } : student);
          this.studentService.saveStudentDirectory(this.students);
          this.loadStudents();
          this.closeForm();
        }
      });
      return;
    }

    this.studentService.registerAuthStudent(normalised).subscribe({
      next: () => {
        this.studentService.createStudent(normalised).subscribe({
          next: () => {
            this.loadStudents();
            this.closeForm();
          },
          error: () => {
            const id = `STD-${Math.random().toString().slice(2, 8)}`;
            this.students.unshift({ ...normalised, id, school: this.normalizeSchoolDisplay(normalised.school) });
            this.studentService.saveStudentDirectory(this.students);
            this.loadStudents();
            this.closeForm();
          }
        });
      },
      error: () => {
        this.studentService.createStudent(normalised).subscribe({
          next: () => {
            this.loadStudents();
            this.closeForm();
          },
          error: () => {
            const id = `STD-${Math.random().toString().slice(2, 8)}`;
            this.students.unshift({ ...normalised, id, school: this.normalizeSchoolDisplay(normalised.school) });
            this.studentService.saveStudentDirectory(this.students);
            this.loadStudents();
            this.closeForm();
          }
        });
      }
    });
  }

  deleteStudent(studentId: string): void {
    this.studentService.deleteStudent(studentId).subscribe({
      next: () => this.loadStudents(),
      error: () => {
        this.students = this.students.filter((student) => student.id !== studentId);
        this.studentService.saveStudentDirectory(this.students);
        this.loadStudents();
      }
    });
  }
}
