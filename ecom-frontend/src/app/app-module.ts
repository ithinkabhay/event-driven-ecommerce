import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';  // <-- add this


import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Login } from './login/login';
import { Products } from './products/products';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { JwtInterceptor } from './core/jwt-interceptor';
import { Header } from './header/header';
import { Cart } from './cart/cart';
import { PaymentsComponent } from './payments/payments';

@NgModule({
  declarations: [
    App,
    Login,
    Products,
    Header,
    Cart,
    PaymentsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    CommonModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    provideBrowserGlobalErrorListeners()
  ],
  bootstrap: [App]
})
export class AppModule { }
