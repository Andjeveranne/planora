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

  // ===================== EXPORT PDF =====================
  exporterPdf(): void {
    if (!this.emploisGeneres.length) {
      this.messages.error = 'Générez d\u2019abord un emploi du temps avant l\u2019export PDF.';
      return;
    }

    const ecoleNom = this.nomEcoleSelectionnee || 'Planora';
    const dept = this.selectedDepartement || 'All departments';
    const spec = this.selectedSpecialite || 'All specialities';
    const dateStr = new Date().toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });

    const slotStyles: Record<string, { bg: string; tc: string }> = {
      cm:  { bg: '#dbeafe', tc: '#1e40af' },
      td:  { bg: '#fef3c7', tc: '#92400e' },
      tp:  { bg: '#dcfce7', tc: '#166534' },
      tpe: { bg: '#f3f4f6', tc: '#6b7280' },
    };

    const getStyle = (typeCours: string) => {
      const k = (typeCours || '').toLowerCase().replace(/\s+/g, '');
      return slotStyles[k] || slotStyles['tpe'];
    };

    const tableHeaders =
      `<th style="width:90px;background:#f1f5f9;color:#334155;font-size:11px;padding:8px 6px;border:1px solid #e2e8f0;">Time slot</th>` +
      this.JOURS.map(j => `<th style="background:#2D3561;color:#fff;font-size:11px;padding:8px 6px;border:1px solid #1e2a4a;">${j.label}</th>`).join('');

    let tablesHtml = '';
    for (const classeId of this.classesIds) {
      const nomClasse = this.grille[classeId].nomClasse;
      let rows = '';
      for (const creneau of this.CRENEAUX) {
        let cells = `<td style="font-size:10px;font-weight:600;background:#f8fafc;color:#475569;padding:8px 6px;border:1px solid #e2e8f0;text-align:center;white-space:nowrap;">${creneau}</td>`;
        for (const jour of this.JOURS) {
          const slot = this.getSlot(classeId, jour.num, creneau);
          if (slot) {
            const { bg, tc } = getStyle(slot.typeCours);
            const typeLabel = this.getTypeLabel(slot.typeCours);
            const isTPE = slot.typeCours === 'TPE';
            const ue   = isTPE ? 'Project Work' : (slot.codeUE || slot.nomUE || '');
            const title = isTPE ? 'Independent study' : (slot.nomUE && slot.nomUE !== ue ? slot.nomUE : '');
            const teacher = isTPE ? '' : (slot.nomEnseignant || '');
            const salle   = isTPE ? '' : (slot.nomSalle || '');
            cells += `<td style="background:${bg};border:1px solid #e2e8f0;padding:6px 4px;vertical-align:top;min-width:100px;">
              <div style="font-size:9px;font-weight:700;color:${tc};text-transform:uppercase;letter-spacing:.4px;">${typeLabel}</div>
              <div style="font-size:10px;font-weight:700;color:#0f172a;margin-top:2px;">${ue}</div>
              ${title  ? `<div style="font-size:9px;color:#374151;margin-top:1px;">${title}</div>` : ''}
              ${teacher ? `<div style="font-size:9px;color:#6b7280;margin-top:3px;">\u{1F464} ${teacher}</div>` : ''}
              ${salle   ? `<div style="font-size:9px;color:#6b7280;">\u{1F4CD} ${salle}</div>` : ''}
            </td>`;
          } else {
            cells += `<td style="background:#fafafa;border:1px solid #e2e8f0;padding:6px;text-align:center;color:#d1d5db;font-size:11px;">—</td>`;
          }
        }
        rows += `<tr>${cells}</tr>`;
      }

      tablesHtml += `
        <div style="margin-bottom:28px;">
          <div style="background:linear-gradient(135deg,#2D3561,#4a5299);color:#fff;padding:10px 16px;border-radius:8px 8px 0 0;font-size:12px;font-weight:700;">
            \u{1F4DA} Class: ${nomClasse}
          </div>
          <table style="width:100%;border-collapse:collapse;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
            <thead><tr>${tableHeaders}</tr></thead>
            <tbody>${rows}</tbody>
          </table>
        </div>`;
    }

    const legendHtml = [
      { k: 'cm', l: 'Lecture',      bg: '#dbeafe', tc: '#1e40af' },
      { k: 'td', l: 'Tutorial',     bg: '#fef3c7', tc: '#92400e' },
      { k: 'tp', l: 'Practical',    bg: '#dcfce7', tc: '#166534' },
      { k: 'tpe', l: 'Project Work', bg: '#f3f4f6', tc: '#6b7280' },
    ].map(x => `<span style="display:inline-flex;align-items:center;gap:5px;background:${x.bg};color:${x.tc};padding:3px 10px;border-radius:20px;font-size:10px;font-weight:600;">${x.l}</span>`).join('');

    const fullHtml = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Timetable – ${ecoleNom}</title>
  <style>
    * { margin:0; padding:0; box-sizing:border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background:#fff; color:#1e293b; }
    @media print {
      @page { size: A4 landscape; margin: 10mm; }
      body { print-color-adjust: exact; -webkit-print-color-adjust: exact; }
    }
  </style>
</head>
<body style="padding:24px;">
  <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:24px;padding-bottom:16px;border-bottom:3px solid #2D3561;">
    <div>
      <div style="font-size:24px;font-weight:800;color:#2D3561;letter-spacing:-0.5px;">PLANORA</div>
      <div style="font-size:13px;color:#64748b;margin-top:2px;">Academic Timetable Management System</div>
    </div>
    <div style="text-align:right;">
      <div style="font-size:18px;font-weight:700;color:#1e293b;">Generated Timetable</div>
      <div style="font-size:11px;color:#64748b;margin-top:4px;">Generated on ${dateStr}</div>
    </div>
  </div>

  <div style="display:flex;gap:12px;flex-wrap:wrap;margin-bottom:20px;">
    <div style="background:#eff6ff;border:1px solid #bfdbfe;padding:8px 16px;border-radius:10px;">
      <div style="font-size:9px;font-weight:700;color:#1e40af;text-transform:uppercase;letter-spacing:.5px;">School</div>
      <div style="font-size:13px;font-weight:700;color:#1e3a8a;margin-top:2px;">${ecoleNom}</div>
    </div>
    <div style="background:#f0fdf4;border:1px solid #bbf7d0;padding:8px 16px;border-radius:10px;">
      <div style="font-size:9px;font-weight:700;color:#166534;text-transform:uppercase;letter-spacing:.5px;">Department</div>
      <div style="font-size:13px;font-weight:700;color:#14532d;margin-top:2px;">${dept}</div>
    </div>
    <div style="background:#fefce8;border:1px solid #fde68a;padding:8px 16px;border-radius:10px;">
      <div style="font-size:9px;font-weight:700;color:#92400e;text-transform:uppercase;letter-spacing:.5px;">Speciality</div>
      <div style="font-size:13px;font-weight:700;color:#78350f;margin-top:2px;">${spec}</div>
    </div>
    <div style="background:#fdf4ff;border:1px solid #e9d5ff;padding:8px 16px;border-radius:10px;">
      <div style="font-size:9px;font-weight:700;color:#7c3aed;text-transform:uppercase;letter-spacing:.5px;">Total slots</div>
      <div style="font-size:13px;font-weight:700;color:#5b21b6;margin-top:2px;">${this.emploisGeneres.length}</div>
    </div>
  </div>

  <div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:20px;">${legendHtml}</div>

  ${tablesHtml}

  <div style="margin-top:24px;padding-top:12px;border-top:1px solid #e2e8f0;display:flex;justify-content:space-between;font-size:9px;color:#9ca3af;">
    <span>PLANORA – Academic Timetable Management System</span>
    <span>Confidential – Internal Use Only</span>
  </div>
</body>
</html>`;

    const printWin = window.open('', '_blank', 'width=1200,height=800');
    if (printWin) {
      printWin.document.write(fullHtml);
      printWin.document.close();
      printWin.focus();
      setTimeout(() => { printWin.print(); }, 800);
    }
  }

  // ===================== EXPORT CSV/EXCEL =====================
  exporterExcel(): void {
    if (!this.emploisGeneres.length) {
      this.messages.error = 'Générez d\u2019abord un emploi du temps avant l\u2019export.';
      return;
    }

    const ecoleNom = this.nomEcoleSelectionnee || '';
    const dept = this.selectedDepartement || '';
    const spec = this.selectedSpecialite || '';
    const dateStr = new Date().toISOString().split('T')[0];

    const esc = (v: string | undefined | null): string => `"${String(v ?? '').replace(/"/g, '""')}"`;

    const BOM = '\uFEFF';
    const rows: string[] = [
      `"PLANORA \u2013 Timetable Export"`,
      `"School",${esc(ecoleNom)}`,
      `"Department",${esc(dept)}`,
      `"Speciality",${esc(spec)}`,
      `"Generated on",${esc(dateStr)}`,
      '',
    ];

    for (const classeId of this.classesIds) {
      const nomClasse = this.grille[classeId].nomClasse;
      rows.push(`"=== Class: ${nomClasse} ==="`);
      rows.push([esc('Time Slot'), ...this.JOURS.map(j => esc(j.label))].join(','));

      for (const creneau of this.CRENEAUX) {
        const cells: string[] = [esc(creneau)];
        for (const jour of this.JOURS) {
          const slot = this.getSlot(classeId, jour.num, creneau);
          if (!slot) {
            cells.push('""');
          } else if (slot.typeCours === 'TPE') {
            cells.push(esc('Project Work'));
          } else {
            const type = this.getTypeLabel(slot.typeCours);
            const ue = slot.codeUE || slot.nomUE || '';
            const nom = (slot.nomUE && slot.nomUE !== ue) ? slot.nomUE : '';
            const teacher = slot.nomEnseignant || '';
            const salle = slot.nomSalle || '';
            const parts = [`[${type}]`, ue, nom, teacher ? `Teacher: ${teacher}` : '', salle ? `Room: ${salle}` : ''].filter(Boolean);
            cells.push(esc(parts.join(' | ')));
          }
        }
        rows.push(cells.join(','));
      }
      rows.push('');
    }

    const csv = BOM + rows.join('\r\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `timetable_${(ecoleNom || 'planora').replace(/\s+/g, '_')}_${dateStr}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
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
