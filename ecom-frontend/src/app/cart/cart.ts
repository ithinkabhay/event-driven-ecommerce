import { Component, OnInit } from '@angular/core';
import { CartItem, CartService } from '../core/cart';
import { OrderService } from '../core/order';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cart',
  standalone: false,
  templateUrl: './cart.html',
  styleUrl: './cart.scss',
})
export class Cart implements OnInit {

  items: CartItem[] = [];
  total = 0;
  message = '';
  error = '';

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.items = this.cartService.getItems();
    this.total = this.cartService.getTotle();
  }

  remove(item: CartItem): void {
    this.cartService.removeFromCart(item.productId);
    this.refresh();
  }

  clear(): void {
    this.cartService.clearCart();
    this.refresh();
  }

  goToPayment(): void {
  if (this.items.length === 0) {
    this.error = "Your cart is empty.";
    return;
  }

  const orderRequest = {
    items: this.items.map(item => ({
      productId: item.productId,
      quantity: item.quantity,
      price: item.price
    })),
    total: this.total
  };

  this.orderService.createOrder(orderRequest).subscribe({
    next: (order) => {
      // Navigate WITH ORDER ID
      this.router.navigate(['/payments'], { queryParams: { orderId: order.id } });
    },
    error: (err) => {
      console.error(err);
      this.error = "Failed to create order.";
    }
  });
}
}
