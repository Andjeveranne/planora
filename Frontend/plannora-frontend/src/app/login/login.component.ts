import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
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

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;
  showPassword: boolean = false;
  private apiUrl = 'http://localhost:8085/api/auth';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
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
          this.errorMessage = error.error?.message || 'Email ou mot de passe incorrect';
        }
      });
    }
  }

  onForgotPassword(): void {
    const emailControl = this.loginForm.get('email');
    if (emailControl?.invalid || !emailControl?.value) {
      this.errorMessage = 'Veuillez entrer une adresse email valide pour réinitialiser le mot de passe.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.http.post<{ message: string }>(`${this.apiUrl}/forgot-password`, { email: emailControl.value }).subscribe({
      next: (response) => {
        this.isLoading = false;
        alert(response.message); // Simple alert for now, can be improved
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

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }
}
