import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface LoginResponse {
  token: string;
  type: string;
  userId: number;
  email: string;
  nom: string;
  prenom: string;
  role: string;
}

interface DomainValidationInfo {
  allowedDomain: string;
  exampleFormat: string;
  message: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;
  showPassword: boolean = false;
  domainInfo: DomainValidationInfo | null = null;
  private apiUrl = 'http://localhost:8085/api/auth';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email, this.institutionalEmailValidator.bind(this)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.loadDomainValidationInfo();
  }

  /**
   * Custom validator for institutional email domain
   */
  institutionalEmailValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Let required validator handle empty values
    }

    const email = control.value.toLowerCase();
    const allowedDomain = '@saintjeaningenieur.org';
    
    if (!email.endsWith(allowedDomain)) {
      return { 
        institutionalEmail: { 
          message: `Seules les adresses email ${allowedDomain} sont autorisées`,
          allowedDomain: allowedDomain
        } 
      };
    }

    // Additional format validation for institutional emails
    const localPart = email.substring(0, email.lastIndexOf('@'));
    
    // Allow admin and system prefixes
    if (localPart.startsWith('admin.') || localPart.startsWith('system.')) {
      return null;
    }
    
    // Standard format: firstname.lastname
    if (!/^[a-z]+\.[a-z]+$/.test(localPart)) {
      return { 
        institutionalEmailFormat: { 
          message: 'Format attendu: prenom.nom@saintjeaningenieur.org',
          example: 'jean.dupont@saintjeaningenieur.org'
        } 
      };
    }

    return null;
  }

  /**
   * Load domain validation info from backend
   */
  loadDomainValidationInfo(): void {
    this.http.get<DomainValidationInfo>(`${this.apiUrl}/domain-validation`).subscribe({
      next: (info) => {
        this.domainInfo = info;
      },
      error: (error) => {
        console.warn('Could not load domain validation info:', error);
        // Set default values if backend is not available
        this.domainInfo = {
          allowedDomain: '@saintjeaningenieur.org',
          exampleFormat: 'prenom.nom@saintjeaningenieur.org',
          message: 'Seules les adresses email institutionnelles sont autorisées'
        };
      }
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      this.http.post<LoginResponse>(`${this.apiUrl}/login`, this.loginForm.value).subscribe({
        next: (response) => {
          this.isLoading = false;
          localStorage.setItem('currentUser', JSON.stringify(response));
          localStorage.setItem('token', response.token);

          console.log('Connexion réussie!', response);

          // Redirection selon le rôle
          if (response.role === 'ADMIN' || response.role === 'ADMINISTRATEUR') {
            this.router.navigate(['/admin-dashboard']);
          } else if (response.role === 'ENSEIGNANT') {
            this.router.navigate(['/enseignant-dashboard']);
          } else {
            alert(`Bienvenue ${response.prenom} ${response.nom}!\nRôle: ${response.role}\nDashboard étudiant à venir...`);
          }
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Erreur de connexion:', error);
          
          // Handle specific domain validation errors
          if (error.error?.message?.includes('saintjeaningenieur.org')) {
            this.errorMessage = error.error.message;
          } else {
            this.errorMessage = error.error?.message || 'Email ou mot de passe incorrect';
          }
        }
      });
    } else {
      // Mark all fields as touched to show validation errors
      this.loginForm.markAllAsTouched();
    }
  }

  onForgotPassword(): void {
    const emailControl = this.loginForm.get('email');
    if (emailControl?.invalid || !emailControl?.value) {
      this.errorMessage = 'Veuillez entrer une adresse email institutionnelle valide pour réinitialiser le mot de passe.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.http.post<{ message: string }>(`${this.apiUrl}/forgot-password`, { email: emailControl.value }).subscribe({
      next: (response) => {
        this.isLoading = false;
        alert(response.message);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Erreur lors de l\'envoi de l\'email';
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Get email validation error message
   */
  getEmailErrorMessage(): string {
    const emailControl = this.email;
    if (emailControl?.errors && emailControl.touched) {
      if (emailControl.errors['required']) {
        return 'L\'adresse email est requise';
      }
      if (emailControl.errors['email']) {
        return 'Format d\'email invalide';
      }
      if (emailControl.errors['institutionalEmail']) {
        return emailControl.errors['institutionalEmail'].message;
      }
      if (emailControl.errors['institutionalEmailFormat']) {
        return emailControl.errors['institutionalEmailFormat'].message;
      }
    }
    return '';
  }

  /**
   * Check if email field has validation errors
   */
  hasEmailError(): boolean {
    const emailControl = this.email;
    return !!(emailControl?.errors && emailControl.touched);
  }

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }
}
