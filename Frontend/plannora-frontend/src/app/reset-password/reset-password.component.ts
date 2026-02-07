import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './reset-password.component.html',
    styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
    resetForm: FormGroup;
    token: string = '';
    errorMessage: string = '';
    successMessage: string = '';
    isLoading: boolean = false;
    showPassword: boolean = false;
    private apiUrl = 'http://localhost:8081/api/auth';

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private http: HttpClient,
        private router: Router
    ) {
        this.resetForm = this.fb.group({
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, { validator: this.passwordMatchValidator });
    }

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParams['token'];
        if (!this.token) {
            this.errorMessage = 'Token invalide ou manquant.';
        }
    }

    passwordMatchValidator(form: FormGroup) {
        const password = form.get('newPassword')?.value;
        const confirmPassword = form.get('confirmPassword')?.value;
        return password === confirmPassword ? null : { mismatch: true };
    }

    onSubmit(): void {
        if (this.resetForm.valid && this.token) {
            this.isLoading = true;
            this.errorMessage = '';
            this.successMessage = '';

            const newPassword = this.resetForm.get('newPassword')?.value;

            this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, {
                token: this.token,
                newPassword: newPassword
            }).subscribe({
                next: (response) => {
                    this.isLoading = false;
                    this.successMessage = response.message;
                    setTimeout(() => {
                        this.router.navigate(['/login']);
                    }, 3000);
                },
                error: (error) => {
                    this.isLoading = false;
                    this.errorMessage = error.error?.message || 'Erreur lors de la réinitialisation';
                }
            });
        }
    }
}
