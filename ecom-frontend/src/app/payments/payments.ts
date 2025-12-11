import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';


import { loadStripe, Stripe, StripeCardElement, StripeElements } from '@stripe/stripe-js';
import { CartService } from '../core/cart';
import { OrderService } from '../core/order';
import { environment } from '../../enviroment/enviroments';

@Component({
  selector: 'app-payments',
  standalone: false,
  templateUrl: './payments.html',
  styleUrls: ['./payments.scss'],
})
export class PaymentsComponent implements OnInit, OnDestroy {
  stripe: Stripe | null = null;
  elements: StripeElements | null = null;
  card: StripeCardElement | null = null;
  isLoading = false;
  error = '';
  success = '';
  total = 0;
  orderId!: number;

  // set your publishable key
  private stripePublishableKey = 'pk_test_51RY9GnFSEtoWGSjPlOihrh8jb26qCIy7DJ7iys0HHuSzVLyXw9PWJftYs2VDNgmmkfxSYl1WOlzPk6R6FL9QzJYQ00c2Drjf3r';

  constructor(
    private cartService: CartService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  async ngOnInit() {
    // get orderId from query params
    this.route.queryParams.subscribe(p => {
      this.orderId = +p['orderId'];
    });

    this.total = this.cartService.getTotle();

    this.stripe = await loadStripe(this.stripePublishableKey);

    if (!this.stripe) {
      this.error = 'Stripe failed to load';
      return;
    }

    this.elements = this.stripe.elements();
    this.card = this.elements.create('card');
    this.card.mount('#card-element');
  }

  ngOnDestroy() {
    if (this.card) this.card.unmount();
  }

  async onPay() {
    this.isLoading = true;
    this.error = '';
    this.success = '';

    if (!this.stripe || !this.card) {
      this.error = 'Stripe not initialized';
      this.isLoading = false;
      return;
    }

    // 1) Ask backend to create PaymentIntent for this orderId
    const body = { amount: this.total, orderId: this.orderId };
    try {
      const res: any = await this.http.post(`${environment.apiBaseUrl}/payments/create-intent`, body).toPromise();
      const clientSecret = res.clientSecret;

      // 2) Confirm card payment on frontend (handles OTP/3DS)
      const result = await this.stripe.confirmCardPayment(clientSecret, {
        payment_method: { card: this.card }
      });

      if (result.error) {
        this.error = result.error.message || 'Payment failed';
        this.isLoading = false;
        return;
      }

      if (result.paymentIntent && result.paymentIntent.status === 'succeeded') {
        // IMPORTANT: With webhook approach you DON'T need to call backend to confirm.
        // Stripe will call webhook -> payment-service -> publish PaymentEvent -> order-service will mark COMPLETED.
        this.success = 'Payment succeeded. Order will be completed shortly.';
        // clear cart if you want
        this.cartService.clearCart();

        // optionally navigate to orders page
        this.router.navigate(['/orders', this.orderId]);
      } else {
        this.error = 'Payment not completed';
      }
    } catch (err: any) {
      console.error(err);
      this.error = err?.message || 'Payment error';
    } finally {
      this.isLoading = false;
    }
  }
}