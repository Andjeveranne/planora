import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, map } from 'rxjs';
import { EmploiDuTempsService, EmploiDuTempsDTO } from '../../services/emploi-du-temps.service';
import { EcoleService, EcoleDTO } from '../../services/ecole.service';
import { ClassesService, FiliereDTO, SpecialiteDTO } from '../../services/classes.service';
import { PlanningService } from '../../services/planning.service';
import { UserService, Enseignant } from '../../services/user.service';

interface EnseignantDispo {
  enseignantId: string;
  nom?: string;
  prenom?: string;
  email?: string;
  departement?: string;
  specialite?: string;
  nombreDisponibilites: number;
}

interface SlotGrid {
  [classeId: string]: {
    nomClasse: string;
    jours: {
      [jour: number]: {
        [creneau: string]: EmploiDuTempsDTO | null;
      };
    };
  };
}

@Component({
  selector: 'app-generation-emplois',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './generation-emplois.component.html',
  styleUrls: ['./generation-emplois.component.css']
})
export class GenerationEmploisComponent implements OnInit {

  ecoles: EcoleDTO[] = [];
  filieres: FiliereDTO[] = [];
  specialitesDisponibles: SpecialiteDTO[] = [];
  selectedEcoleId: string = '';
  selectedDepartement = '';
  selectedSpecialite = '';
  enseignantsDispo: EnseignantDispo[] = [];

  isLoadingEcoles = false;
  isLoadingEnseignants = false;
  isGenerating = false;

  emploisGeneres: EmploiDuTempsDTO[] = [];
  grille: SlotGrid = {};
  classesIds: string[] = [];

  messages = { success: '', error: '' };

  readonly JOURS = [
    { num: 1, label: 'Monday' },
    { num: 2, label: 'Tuesday' },
    { num: 3, label: 'Wednesday' },
    { num: 4, label: 'Thursday' },
    { num: 5, label: 'Friday' }
  ];

  readonly CRENEAUX = ['8:00-10:00', '10:00-12:00', '12:00-14:00', '14:00-16:00', '16:00-18:00'];

  /** Normalise any créneau format to the canonical display format used by CRENEAUX. */
  private normalizeCreneau(raw: string): string {
    if (!raw) return raw;
    // "8h-10h"  → "8:00-10:00"
    // "10h-12h" → "10:00-12:00"
    return raw.replace(/(\d{1,2})h-(\d{1,2})h/, (_m, h1: string, h2: string) =>
      `${h1.padStart(1, '0')}:00-${h2.padStart(1, '0')}:00`
    );
  }

  constructor(
    private emploiDuTempsService: EmploiDuTempsService,
    private ecoleService: EcoleService,
    private classesService: ClassesService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadEcoles();
    this.classesService.getFilieres().subscribe({
      next: (filieres) => this.filieres = filieres,
      error: () => this.messages.error = 'Impossible de charger les départements.'
    });
    this.classesService.getSpecialites().subscribe({
      next: (specialites) => this.specialitesDisponibles = specialites,
      error: () => this.messages.error = 'Impossible de charger les spécialités.'
    });
    this.loadEnseignantsDispo();
  }

  loadEcoles(): void {
    this.isLoadingEcoles = true;
    this.ecoleService.obtenirToutes().subscribe({
      next: (ecoles) => {
        this.ecoles = ecoles;
        if (ecoles.length > 0 && !this.selectedEcoleId) {
          this.selectedEcoleId = ecoles[0].idEcole ?? '';
        }
        this.isLoadingEcoles = false;
        this.loadEnseignantsDispo();
      },
      error: () => {
        this.messages.error = 'Impossible de charger les écoles.';
        this.isLoadingEcoles = false;
      }
    });
  }

  loadEnseignantsDispo(): void {
    this.isLoadingEnseignants = true;
    this.messages = { success: '', error: '' };

    const planningRequest = this.emploiDuTempsService.getEnseignantsAvecDisponibilites(
      this.selectedEcoleId || undefined,
      this.selectedDepartement || undefined,
      this.selectedSpecialite || undefined
    );

    const teacherRequest = this.userService.rechercherEnseignants(
      this.selectedEcoleId || undefined,
      this.selectedDepartement || undefined,
      this.selectedSpecialite || undefined
    );

    this.emploiDuTempsService.getEnseignantsAvecDisponibilites(
      this.selectedEcoleId || undefined,
      this.selectedDepartement || undefined,
      this.selectedSpecialite || undefined
    ).subscribe({
      next: (data: any[]) => {
        const fromPlanning = (data ?? []).map(d => ({
          enseignantId: d.enseignantId ?? d.idUser ?? '',
          nom: d.nom ?? d.nomUser ?? '',
          prenom: d.prenom ?? d.prenomUser ?? '',
          email: d.email ?? '',
          departement: d.departement ?? '',
          specialite: d.specialite ?? '',
          nombreDisponibilites: Number(d.nombreDisponibilites ?? d.nbDisponibilites ?? 0)
        })).filter(ens => ens.enseignantId);

        if (fromPlanning.length > 0) {
          this.enseignantsDispo = fromPlanning;
          this.isLoadingEnseignants = false;
          return;
        }

        teacherRequest.subscribe({
          next: (teachers) => {
            this.enseignantsDispo = (teachers ?? []).map(t => ({
              enseignantId: t.idUser ?? '',
              nom: t.nomUser ?? '',
              prenom: t.prenomUser ?? '',
              email: t.email ?? '',
              departement: t.departement ?? '',
              specialite: t.specialite ?? '',
              nombreDisponibilites: 0
            })).filter(ens => ens.enseignantId);
            this.isLoadingEnseignants = false;
          },
          error: () => {
            this.enseignantsDispo = [];
            this.messages.error = 'Impossible de charger la liste des enseignants.';
            this.isLoadingEnseignants = false;
          }
        });
      },
      error: () => {
        teacherRequest.subscribe({
          next: (teachers) => {
            this.enseignantsDispo = (teachers ?? []).map(t => ({
              enseignantId: t.idUser ?? '',
              nom: t.nomUser ?? '',
              prenom: t.prenomUser ?? '',
              email: t.email ?? '',
              departement: t.departement ?? '',
              specialite: t.specialite ?? '',
              nombreDisponibilites: 0
            })).filter(ens => ens.enseignantId);
            this.isLoadingEnseignants = false;
          },
          error: () => {
            this.enseignantsDispo = [];
            this.messages.error = 'Impossible de charger la liste des enseignants.';
            this.isLoadingEnseignants = false;
          }
        });
      }
    });
  }

  onSchoolChange(): void {
    this.selectedDepartement = '';
    this.selectedSpecialite = '';
    this.loadEnseignantsDispo();
  }

  onDepartmentChange(): void {
    this.selectedSpecialite = '';
    this.loadEnseignantsDispo();
  }

  get departements(): string[] {
    return this.filieres
      .filter(filiere => !this.selectedEcoleId || filiere.idEcole === this.selectedEcoleId)
      .map(filiere => filiere.nom)
      .sort((a, b) => a.localeCompare(b));
  }

  get specialites(): string[] {
    const department = this.filieres.find(filiere =>
      filiere.nom === this.selectedDepartement &&
      (!this.selectedEcoleId || filiere.idEcole === this.selectedEcoleId)
    );
    return this.specialitesDisponibles
      .filter(specialite => specialite.idFiliere === department?.idFiliere)
      .map(specialite => specialite.nom)
      .sort((a, b) => a.localeCompare(b));
  }

  generer(): void {
    this.isGenerating = true;
    this.messages = { success: '', error: '' };
    this.emploiDuTempsService.genererEmploisDuTemps(
      this.selectedEcoleId || undefined,
      this.selectedDepartement || undefined,
      this.selectedSpecialite || undefined
    ).subscribe({
      next: (emplois) => {
        const cours = emplois.filter(emploi => emploi.typeCours !== 'TPE');
        if (cours.length === 0) {
          this.emploisGeneres = [];
          this.grille = {};
          this.classesIds = [];
          this.isGenerating = false;
          this.messages.error = 'Aucun cours n’a pu être planifié. Vérifiez les UE, les affectations des enseignants, leurs disponibilités et les salles de cette école.';
          return;
        }
        this.emploisGeneres = emplois;
        this.buildGrille(emplois);
        this.isGenerating = false;
        this.messages.success = `${emplois.length} créneaux générés avec succès pour la semaine à venir.`;
        setTimeout(() => { this.messages.success = ''; }, 4000);
      },
      error: (err) => {
        this.messages.error = err.error?.message || 'Erreur lors de la génération.';
        this.isGenerating = false;
      }
    });
  }

  exporterPdf(): void {
    if (!this.emploisGeneres.length) {
      this.messages.error = 'Générez d’abord un emploi du temps avant l’export PDF.';
      return;
    }
    window.print();
  }

  private buildGrille(emplois: EmploiDuTempsDTO[]): void {
    this.grille = {};
    this.classesIds = [];

    for (const e of emplois) {
      if (!this.grille[e.classeId]) {
        this.grille[e.classeId] = { nomClasse: e.nomClasse ?? e.classeId, jours: {} };
        this.classesIds.push(e.classeId);
      }
      const entry = this.grille[e.classeId];
      if (!entry.jours[e.jourSemaine]) entry.jours[e.jourSemaine] = {};
      entry.jours[e.jourSemaine][this.normalizeCreneau(e.creneau)] = e;
    }

    this.classesIds.sort((a, b) =>
      (this.grille[a].nomClasse).localeCompare(this.grille[b].nomClasse));
  }

  getSlot(classeId: string, jour: number, creneau: string): EmploiDuTempsDTO | null {
    return this.grille[classeId]?.jours?.[jour]?.[creneau] ?? null;
  }

  getSlotClass(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return 'slot-empty';
    const t = (slot.typeCours ?? '').toLowerCase();
    if (t === 'tpe' || t === 'project work' || t === 'guided project') return 'slot-tpe';
    if (t === 'cm' || t === 'lecture') return 'slot-cm';
    if (t === 'td' || t === 'tutorial') return 'slot-td';
    if (t === 'tp' || t === 'practical' || t === 'practical lab') return 'slot-tp';
    return 'slot-other';
  }

  getTypeLabel(typeCours?: string): string {
    const normalized = (typeCours ?? '').trim().toLowerCase();
    const labels: Record<string, string> = {
      cm: 'Lecture',
      lecture: 'Lecture',
      td: 'Tutorial',
      tutorial: 'Tutorial',
      tp: 'Practical',
      practical: 'Practical',
      practicallab: 'Practical',
      tpe: 'Project Work',
      'project work': 'Project Work',
      'guided project': 'Project Work'
    };
    return labels[normalized] ?? (typeCours ?? 'Course');
  }

  getCourseDisplay(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return '';
    return slot.codeUE || slot.nomUE || 'Course';
  }

  getCourseTitle(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return '';
    return slot.nomUE || slot.codeUE || 'Course';
  }

  getTeacherDisplay(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return '';
    return slot.nomEnseignant || slot.enseignantId || 'Teacher';
  }

  getSlotLabel(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return '';
    return `${this.getTypeLabel(slot.typeCours)} • ${this.getCourseDisplay(slot)}`;
  }

  getSlotTooltip(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return '';
    const teacher = this.getTeacherDisplay(slot);
    return [
      this.getTypeLabel(slot.typeCours),
      slot.codeUE ? `UE: ${slot.codeUE} – ${slot.nomUE ?? ''}` : '',
      teacher ? `Teacher: ${teacher}` : '',
      slot.nomSalle ? `Room: ${slot.nomSalle}` : ''
    ].filter(Boolean).join('\n');
  }

  get nombreCM(): number { return this.emploisGeneres.filter(e => this.getTypeLabel(e.typeCours) === 'Lecture').length; }
  get nombreTD(): number { return this.emploisGeneres.filter(e => this.getTypeLabel(e.typeCours) === 'Tutorial').length; }
  get nombreTP(): number { return this.emploisGeneres.filter(e => this.getTypeLabel(e.typeCours) === 'Practical').length; }
  get nombreTPE(): number { return this.emploisGeneres.filter(e => this.getTypeLabel(e.typeCours) === 'Project Work').length; }

  get nomEcoleSelectionnee(): string {
    return this.ecoles.find(e => e.idEcole === this.selectedEcoleId)?.nomEcole ?? '';
  }
}
