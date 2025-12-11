import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './login/login';
import { Products } from './products/products';
import { Cart } from './cart/cart';
import { PaymentsComponent } from './payments/payments';


const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'products', component: Products },
  { path: 'cart', component: Cart},
  { path: 'payments', component: PaymentsComponent},
  { path: '', redirectTo: '/products', pathMatch: 'full' }
];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
