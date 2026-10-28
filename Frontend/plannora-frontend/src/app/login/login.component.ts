import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NotificationService } from '../services/notification.service';

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
  private apiUrl = 'http://localhost:8081/api/auth';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private notification: NotificationService
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
    const allowedDomain = '@ictuniversity.org';
    
    if (!email.endsWith(allowedDomain)) {
      return { 
        institutionalEmail: { 
          message: `Only email addresses ending in ${allowedDomain} are allowed`,
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
          message: 'Expected format: firstname.lastname@ictuniversity.org',
          example: 'jean.dupont@ictuniversity.org'
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
          allowedDomain: '@ictuniversity.org',
          exampleFormat: 'firstname.lastname@ictuniversity.org',
          message: 'Only institutional email addresses are allowed'
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

          console.log('Login successful!', response);

          // Redirect by role
          if (response.role === 'ADMIN' || response.role === 'ADMINISTRATEUR') {
            this.router.navigate(['/admin-dashboard']);
          } else if (response.role === 'ETUDIANT') {
            this.router.navigate(['/student-dashboard']);
          } else if (response.role === 'ENSEIGNANT') {
            this.router.navigate(['/enseignant-dashboard']);
          } else {
            this.notification.info(`Welcome ${response.prenom} ${response.nom}!\nRole: ${response.role}\nStudent dashboard coming soon...`, 'Login successful');
          }
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Login error:', error);
          
          // Handle specific domain validation errors
          if (error.error?.message?.includes('ictuniversity.org')) {
            this.errorMessage = error.error.message;
          } else {
            this.errorMessage = error.error?.message || 'Invalid email or password';
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
      this.errorMessage = 'Please enter a valid institutional email address to reset your password.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.http.post<{ message: string }>(`${this.apiUrl}/forgot-password`, { email: emailControl.value }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.notification.success(response.message);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Error sending password reset email';
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
        return 'Email address is required';
      }
      if (emailControl.errors['email']) {
        return 'Invalid email format';
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
