import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CourseService, CourseDTO, ClasseDTO } from '../services/course.service';
import { UEService, UEDTO } from '../services/ue.service';
import { UserService } from '../services/user.service';

interface ClasseCourses {
  classe: ClasseDTO;
  courses: CourseDTO[];
  totalHeures: number;
  totalCredits: number;
}

@Component({
  selector: 'app-teacher-courses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './teacher-courses.component.html',
  styleUrls: ['./teacher-courses.component.css']
})
export class TeacherCoursesComponent implements OnInit {
  classesCourses: ClasseCourses[] = [];
  teacherUEs: UEDTO[] = [];
  loading = false;
  error: string | null = null;
  currentTeacherId: string | null = null;
  
  // View mode
  viewMode: 'courses' | 'ues' = 'courses';
  
  // Statistiques globales
  totalClasses = 0;
  totalCourses = 0;
  totalUEs = 0;
  totalHeures = 0;
  
  // Filtres
  selectedNiveau = '';
  selectedSemestre = '';
  searchTerm = '';
  
  // Options de filtre
  niveaux = ['L1', 'L2', 'L3', 'M1', 'M2'];
  semestres = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8'];

  constructor(
    private courseService: CourseService,
    private ueService: UEService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.getCurrentTeacher();
  }

  /**
   * Récupère l'enseignant connecté
   */
  getCurrentTeacher(): void {
    try {
      const currentUser = this.userService.getCurrentUser();
      
      if (currentUser && currentUser.role === 'ENSEIGNANT') {
        this.currentTeacherId = currentUser.idUser;
        this.loadTeacherCourses();
      } else {
        this.error = 'Vous devez être connecté en tant qu\'enseignant pour accéder à cette page.';
      }
    } catch (error) {
      console.error('Erreur lors de la récupération de l\'utilisateur connecté:', error);
      this.error = 'Erreur lors de la récupération des informations utilisateur.';
    }
  }

  /**
   * Charge les cours de l'enseignant groupés par classe
   */
  loadTeacherCourses(): void {
    if (!this.currentTeacherId) return;

    this.loading = true;
    this.error = null;

    // Charger à la fois les cours et les UEs
    Promise.all([
      this.loadCoursesByClasses(),
      this.loadTeacherUEs()
    ]).then(() => {
      this.calculateStats();
      this.loading = false;
    }).catch(error => {
      console.error('Erreur lors du chargement des données:', error);
      this.error = 'Erreur lors du chargement des données.';
      this.loading = false;
    });
  }

  /**
   * Charge les cours par classes
   */
  private loadCoursesByClasses(): Promise<void> {
    return new Promise((resolve, reject) => {
      // Récupérer les classes enseignées par l'enseignant
      this.courseService.getClassesByEnseignant(this.currentTeacherId!).subscribe({
        next: (classes) => {
          this.loadCoursesForClasses(classes);
          resolve();
        },
        error: (error) => {
          console.error('Erreur lors du chargement des classes:', error);
          // Ne pas rejeter, juste résoudre avec des données vides
          this.classesCourses = [];
          resolve();
        }
      });
    });
  }

  /**
   * Charge les UEs de l'enseignant
   */
  private loadTeacherUEs(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.ueService.getUEsByEnseignant(this.currentTeacherId!).subscribe({
        next: (ues) => {
          this.teacherUEs = ues;
          resolve();
        },
        error: (error) => {
          console.error('Erreur lors du chargement des UEs:', error);
          this.teacherUEs = [];
          resolve();
        }
      });
    });
  }

  /**
   * Charge les cours pour chaque classe
   */
  loadCoursesForClasses(classes: ClasseDTO[]): void {
    if (!this.currentTeacherId || classes.length === 0) {
      this.classesCourses = [];
      this.calculateStats();
      this.loading = false;
      return;
    }

    // Process classes one by one to avoid Promise.all with toPromise
    this.classesCourses = [];
    let processedCount = 0;

    classes.forEach(classe => {
      this.courseService.getCoursesByEnseignantAndClasse(this.currentTeacherId!, classe.id!)
        .subscribe({
          next: (courses) => {
            if (courses && courses.length > 0) {
              this.classesCourses.push({
                classe,
                courses: courses,
                totalHeures: courses.reduce((sum, course) => sum + course.nombreHeures, 0),
                totalCredits: 0
              });
            }
            
            processedCount++;
            if (processedCount === classes.length) {
              this.calculateStats();
              this.loading = false;
            }
          },
          error: (error) => {
            console.error('Erreur lors du chargement des cours pour la classe:', classe.nomClasse, error);
            processedCount++;
            if (processedCount === classes.length) {
              this.calculateStats();
              this.loading = false;
            }
          }
        });
    });
  }

  /**
   * Calcule les statistiques globales
   */
  calculateStats(): void {
    this.totalClasses = this.classesCourses.length;
    this.totalCourses = this.classesCourses.reduce((sum, cc) => sum + cc.courses.length, 0);
    this.totalUEs = this.teacherUEs.length;
    this.totalHeures = this.classesCourses.reduce((sum, cc) => sum + cc.totalHeures, 0) +
                      this.teacherUEs.reduce((sum, ue) => sum + this.getTotalHeuresUE(ue), 0);
  }

  /**
   * Filtre les classes et cours selon les critères sélectionnés
   */
  get filteredClassesCourses(): ClasseCourses[] {
    return this.classesCourses.filter(cc => {
      // Filtre par niveau
      if (this.selectedNiveau && cc.classe.niveau !== this.selectedNiveau) {
        return false;
      }

      // Filtre par semestre (au moins un cours du semestre)
      if (this.selectedSemestre) {
        const hasCourseSemestre = cc.courses.some(course => 
          course.semestre === this.selectedSemestre
        );
        if (!hasCourseSemestre) return false;
      }

      // Filtre par terme de recherche
      if (this.searchTerm) {
        const searchLower = this.searchTerm.toLowerCase();
        const matchesClasse = cc.classe.nomClasse.toLowerCase().includes(searchLower) ||
                             cc.classe.filiere.toLowerCase().includes(searchLower);
        const matchesCourse = cc.courses.some(course =>
          course.codeCourse.toLowerCase().includes(searchLower) ||
          (course.ueCode && course.ueCode.toLowerCase().includes(searchLower)) ||
          (course.ueNom && course.ueNom.toLowerCase().includes(searchLower))
        );
        if (!matchesClasse && !matchesCourse) return false;
      }

      return true;
    }).map(cc => ({
      ...cc,
      courses: this.selectedSemestre ? 
        cc.courses.filter(course => course.semestre === this.selectedSemestre) :
        cc.courses
    }));
  }

  /**
   * Réinitialise les filtres
   */
  clearFilters(): void {
    this.selectedNiveau = '';
    this.selectedSemestre = '';
    this.searchTerm = '';
  }

  /**
   * Actualise les données
   */
  refresh(): void {
    this.loadTeacherCourses();
  }

  /**
   * Formate le type de cours avec une couleur
   */
  getCourseTypeClass(typeCours: string): string {
    switch (typeCours) {
      case 'CM': return 'course-type-cm';
      case 'TD': return 'course-type-td';
      case 'TP': return 'course-type-tp';
      default: return 'course-type-default';
    }
  }

  /**
   * Formate le niveau avec une couleur
   */
  getNiveauClass(niveau: string): string {
    switch (niveau) {
      case 'L1': return 'niveau-l1';
      case 'L2': return 'niveau-l2';
      case 'L3': return 'niveau-l3';
      case 'M1': return 'niveau-m1';
      case 'M2': return 'niveau-m2';
      default: return 'niveau-default';
    }
  }

  /**
   * Retourne le nom complet du cours
   */
  getCourseNomComplet(course: CourseDTO): string {
    return this.courseService.formatCourseNomComplet(course);
  }

  /**
   * Retourne les informations de la classe
   */
  getClasseInfo(classe: ClasseDTO): string {
    return `${classe.nomClasse} - ${classe.niveau} (${classe.filiere})`;
  }

  /**
   * Vérifie si une classe a des cours
   */
  hasCoursesInSemestre(courses: CourseDTO[], semestre: string): boolean {
    return courses.some(course => course.semestre === semestre);
  }

  /**
   * Groupe les cours par semestre pour une classe
   */
  getCoursesBySemestre(courses: CourseDTO[]): { [semestre: string]: CourseDTO[] } {
    return courses.reduce((acc, course) => {
      if (!acc[course.semestre]) {
        acc[course.semestre] = [];
      }
      acc[course.semestre].push(course);
      return acc;
    }, {} as { [semestre: string]: CourseDTO[] });
  }

  /**
   * Retourne les semestres uniques pour une classe
   */
  getUniqueSemestres(courses: CourseDTO[]): string[] {
    const semestres = [...new Set(courses.map(course => course.semestre))];
    return semestres.sort();
  }

  /**
   * TrackBy function for classes
   */
  trackByClasseId(_index: number, item: ClasseCourses): any {
    return item.classe.id;
  }

  /**
   * TrackBy function for courses
   */
  trackByCourseId(_index: number, item: CourseDTO): any {
    return item.id;
  }

  /**
   * Bascule entre les modes d'affichage
   */
  setViewMode(mode: 'courses' | 'ues'): void {
    this.viewMode = mode;
  }

  /**
   * Retourne le total des heures pour une UE
   */
  getTotalHeuresUE(ue: UEDTO): number {
    return (ue.heuresCM || 0) + (ue.heuresTD || 0) + (ue.heuresTP || 0);
  }

  /**
   * Retourne le type de cours dominant pour une UE
   */
  getTypeCoursDominantUE(ue: UEDTO): string {
    const cm = ue.heuresCM || 0;
    const td = ue.heuresTD || 0;
    const tp = ue.heuresTP || 0;

    if (cm >= td && cm >= tp) return 'CM';
    if (td >= tp) return 'TD';
    return 'TP';
  }

  /**
   * Filtre les UEs selon les critères sélectionnés
   */
  get filteredUEs(): UEDTO[] {
    return this.teacherUEs.filter(ue => {
      // Filtre par niveau
      if (this.selectedNiveau && ue.niveau !== this.selectedNiveau) {
        return false;
      }

      // Filtre par semestre
      if (this.selectedSemestre && ue.semestre !== this.selectedSemestre) {
        return false;
      }

      // Filtre par terme de recherche
      if (this.searchTerm) {
        const searchLower = this.searchTerm.toLowerCase();
        const matches = ue.codeUE.toLowerCase().includes(searchLower) ||
                       ue.nomUE.toLowerCase().includes(searchLower) ||
                       (ue.description && ue.description.toLowerCase().includes(searchLower));
        if (!matches) return false;
      }

      return true;
    });
  }

  /**
   * TrackBy function for UEs
   */
  trackByUEId(_index: number, item: UEDTO): any {
    return item.id;
  }
}