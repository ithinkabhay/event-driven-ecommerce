import { Component, OnInit } from '@angular/core';
import { AuthService } from '../core/auth';
import { CartService } from '../core/cart';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header implements OnInit {

  cartItemCount: number = 0;

  constructor(
    private cartService: CartService,
    public auth: AuthService,
    private router: Router
  
  ) {}


  ngOnInit(): void {
    this.cartService.itemCount$.subscribe(count => this.cartItemCount = count);
  }

  goToCart(): void {
    this.router.navigate(['/cart']);
  }

  goToProducts(): void {
    this.router.navigate(['/products']);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}


