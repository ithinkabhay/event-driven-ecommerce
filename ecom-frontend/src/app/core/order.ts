import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../enviroment/enviroments';
import { Observable } from 'rxjs';

interface OrderItemRequest {
  productId: number;
  quantity: number;
  price: number;
}

interface CreateOrderRequest {
  items: OrderItemRequest[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  createOrder(req: CreateOrderRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/orders`, req);
  }

  getOrder(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/orders/${id}`);
  }
}