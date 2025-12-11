import { Injectable } from '@angular/core';
import { Product } from './product';
import { BehaviorSubject } from 'rxjs';

export interface CartItem {
  id: any;
  productId: number;
  name: string;
  price: number;
  quantity: number;
}

@Injectable({
  providedIn: 'root',
})
export class CartService {

  private items: CartItem[] = [];

  private itemCountSubject = new BehaviorSubject<number>(0);
  itemCount$ = this.itemCountSubject.asObservable();

  constructor() { }

  getItems(): CartItem[]{
    return this.items;
  }
  
  getTotle(): number {
    return this.items.reduce((total, item) => total + item.price * item.quantity, 0); 
  }

  addToCart(product: Product, quantity: number = 1): void {
    const existing = this.items.find(i => i.productId === product.id);
    if (existing) {
      existing.quantity += quantity;
    } else {
      this.items.push({
        productId: product.id,
        name: product.name,
        price: product.price,
        quantity,
        id: undefined
      });
  }
  this.updateCount();
}

removeFromCart(productId: number): void {
  this.items = this.items.filter(i => i.productId !== productId);
  this.updateCount();
}

clearCart(): void {
  this.items = [];
  this.updateCount();
}

private updateCount(): void {
  const count = this.items.reduce((sum, item) => sum + item.quantity, 0);
  this.itemCountSubject.next(count);
}

}
