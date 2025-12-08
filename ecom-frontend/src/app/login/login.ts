import { Component } from '@angular/core';
import { AuthService } from '../core/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  username = '';
  password = '';
  error = '';
  
  constructor(private auth: AuthService, private router: Router) {}

  onLogin() {
    this.error = '';
    this.auth.login({ username: this.username, password: this.password })
    .subscribe({
      next: () => {
        this.router.navigate(['/products']);
      },
      error: (err) => {
        this.error = 'Login failed. Please check your credentials.';
        console.error('Login error:', err);
      }
    });
  }

}
