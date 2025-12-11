import { Component, OnInit } from '@angular/core';
import { Product, ProductService } from '../core/product';
import { OrderService } from '../core/order';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth';
import { CartService } from '../core/cart';

@Component({
  selector: 'app-products',
  standalone: false,
  templateUrl: './products.html',
  styleUrl: './products.scss',
})
export class Products implements OnInit {


  products: Product[] = [];

  message = '';

  error = '';

  constructor(
    private productService: ProductService,
    // private orderService: OrderService,
    private cartService: CartService,
    private auth: AuthService,
    private router: Router
  ) {}
  

  ngOnInit(): void {
    if(!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadProducts();
  }

  loadProducts() {
    this.productService.getProducts().subscribe({
      next: prods => this.products = prods,
      error: err => {
        console.error(err);
        this.error = 'Failed to load products';
      }
    });
}

// buy(p: Product) {
//     this.message = '';
//     this.error = '';

//     const req = {
//       items: [
//         {
//           productId: p.id,
//           quantity: 1,
//           price: p.price
//         }
//       ]
//     };

//     this.orderService.createOrder(req).subscribe({
//       next: (order) => {
//         this.message = `Order ${order.id} created. Status: ${order.status}`;
//       },
//       error: err => {
//         console.error(err);
//         this.error = 'Failed to create order';
//       }
//     });
//   }

addToCart(p: Product): void {
    this.cartService.addToCart(p, 1);
    this.message = `${p.name} added to cart`;
    this.error = '';
  }
}
