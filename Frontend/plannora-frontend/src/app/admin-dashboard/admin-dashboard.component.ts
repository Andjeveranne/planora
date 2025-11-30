import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { UserService, Enseignant, EnseignantRequest } from '../services/user.service';

interface UserInfo {
  nom: string;
  prenom: string;
  email: string;
  role: string;
}

interface StatCard {
  title: string;
  value: number;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  userInfo: UserInfo | null = null;
  activeSection: string = 'overview';
  recentUsers: any[] = [];
  recentRooms: any[] = [];
  enseignants: Enseignant[] = [];
  showAddEnseignantForm: boolean = false;
  isLoadingEnseignants: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  newEnseignant: EnseignantRequest = {
    email: '',
    mdp: '',
    nomUser: '',
    prenomUser: '',
    telephone: '',
    role: 'ENSEIGNANT',
    specialite: '',
    departement: ''
  };

  constructor(
    private authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userInfo = this.authService.getCurrentUser();

    // Données de démonstration
    this.recentUsers = [
      { name: 'Jesse Thomas', points: '637 Points', accuracy: '98% Correct', initials: 'JT', color: '#667eea' },
      { name: 'Thisal Mathiyazhagan', points: '637 Points', accuracy: '89% Correct', initials: 'TM', color: '#f093fb' },
      { name: 'Helen Chuang', points: '637 Points', accuracy: '88% Correct', initials: 'HC', color: '#4facfe' },
      { name: 'Lura Silverman', points: '637 Points', accuracy: '', initials: 'LS', color: '#43e97b' },
      { name: 'Winifred Groton', points: '637 Points', accuracy: '', initials: 'WG', color: '#f59e0b' }
    ];

    this.recentRooms = [
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: 'Laboratoire' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: 'Travaux Pratiques' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: 'Transversale' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: '' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: '' }
    ];
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'enseignants') {
      this.loadEnseignants();
    }
  }

  loadEnseignants(): void {
    this.isLoadingEnseignants = true;
    this.errorMessage = '';
    
    console.log('🔍 Chargement des enseignants...');
    console.log('Token:', localStorage.getItem('token')?.substring(0, 20) + '...');
    
    this.userService.getEnseignants().subscribe({
      next: (data) => {
        console.log('✅ Enseignants chargés:', data);
        this.enseignants = data;
        this.isLoadingEnseignants = false;
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des enseignants:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        console.error('Error object:', error.error);
        
        let errorMsg = 'Erreur lors du chargement des enseignants';
        if (error.status === 401) {
          errorMsg = 'Non autorisé - Veuillez vous reconnecter';
        } else if (error.status === 403) {
          errorMsg = 'Accès refusé - Permissions insuffisantes';
        } else if (error.status === 404) {
          errorMsg = 'Service non trouvé - Vérifiez que le UserService est démarré';
        } else if (error.status === 0) {
          errorMsg = 'Impossible de contacter le serveur - Vérifiez que les services sont démarrés';
        }
        
        this.errorMessage = errorMsg;
        this.isLoadingEnseignants = false;
      }
    });
  }

  toggleAddEnseignantForm(): void {
    this.showAddEnseignantForm = !this.showAddEnseignantForm;
    if (!this.showAddEnseignantForm) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.newEnseignant = {
      email: '',
      mdp: '',
      nomUser: '',
      prenomUser: '',
      telephone: '',
      role: 'ENSEIGNANT',
      specialite: '',
      departement: ''
    };
    this.errorMessage = '';
    this.successMessage = '';
  }

  ajouterEnseignant(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.validateForm()) {
      return;
    }

    console.log('📝 Ajout d\'un enseignant:', this.newEnseignant);

    this.userService.creerEnseignant(this.newEnseignant).subscribe({
      next: (enseignant) => {
        console.log('✅ Enseignant créé:', enseignant);
        this.successMessage = 'Enseignant ajouté avec succès!';
        this.enseignants.push(enseignant);
        this.resetForm();
        this.showAddEnseignantForm = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('❌ Erreur lors de l\'ajout:', error);
        console.error('Status:', error.status);
        console.error('Error object:', error.error);
        
        let errorMsg = 'Erreur lors de l\'ajout de l\'enseignant';
        if (error.status === 401) {
          errorMsg = 'Non autorisé - Veuillez vous reconnecter';
        } else if (error.status === 403) {
          errorMsg = 'Accès refusé - Permissions insuffisantes';
        } else if (error.error?.message) {
          errorMsg = error.error.message;
        }
        
        this.errorMessage = errorMsg;
      }
    });
  }

  validateForm(): boolean {
    if (!this.newEnseignant.email || !this.newEnseignant.mdp || 
        !this.newEnseignant.nomUser || !this.newEnseignant.prenomUser || 
        !this.newEnseignant.telephone) {
      this.errorMessage = 'Tous les champs obligatoires doivent être remplis';
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.newEnseignant.email)) {
      this.errorMessage = 'Email invalide';
      return false;
    }

    if (this.newEnseignant.mdp.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
      return false;
    }

    return true;
  }

  deleteEnseignant(id: string): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet enseignant?')) {
      return;
    }

    this.userService.deleteEnseignant(id).subscribe({
      next: () => {
        this.enseignants = this.enseignants.filter(e => e.idUser !== id);
        this.successMessage = 'Enseignant supprimé avec succès!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
        this.errorMessage = 'Erreur lors de la suppression de l\'enseignant';
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
