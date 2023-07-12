import { Component, OnInit, Inject, ElementRef } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';

import { catchError } from 'rxjs/operators';
import { AuthenticationService } from 'src/app/_service/authentication.service';
import { throwError } from 'rxjs';
import { User } from 'src/app/_model/user';
import { SocialAuthService, GoogleLoginProvider } from '@abacritt/angularx-social-login';

@Component({
  selector: 'app-signin',
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.css']
})
export class Signin implements OnInit {

  loginForm: FormGroup;
  loading = false;
  submitted = false;
  returnUrl: string;
  isLoggedin?: boolean;
  error = '';

  constructor(
    @Inject(DOCUMENT) public document,
    public elementRef: ElementRef,
    private formBuilder: FormBuilder,
    private router: Router,
    private authenticationService: AuthenticationService,
    private toastr: ToastrService,
    private googleAuthService: SocialAuthService
  ) {
    if (this.authenticationService.currentUserValue) {
      this.router.navigate(['/']);
    }
  }

  ngOnInit() {
    this.authenticationService.saveUserInfo(null);

    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    this.googleAuthService.authState.subscribe((user) => {
      this.siginOrSinupWithGoogle({
        name: user.name, 
        email: user.email, 
        userGoogleId: user.id,
        googleIdToken: user.idToken
      })
    });
  }

  siginOrSinupWithGoogle(user){
    
    this.authenticationService.siginOrSignupWithGoogle(user).subscribe({
      next: (value) => {
        this.onLoginSuccess(value);
      },
      error: (err) => {
        console.log(err)
        this.loading = false
        this.error = err.message
        return throwError(() => err.message)
      }
    })
  }

  get controls() { return this.loginForm.controls; }

  signInWithGoogle(): void {
    this.googleAuthService.signIn(GoogleLoginProvider.PROVIDER_ID);
  }

  onSubmit() {
    this.submitted = true;

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.authenticationService.signin(this.controls.username.value, this.controls.password.value)
      .pipe(catchError(respError => {
        this.loading = false;
        this.error = respError.message;
        return throwError(() => respError.message);
      }))
      .subscribe(data => {
        this.onLoginSuccess(data)
      },

      );
  }

  onLoginSuccess(data) {
    this.loading = false;
    if (data.token != null) {
      this.loading = false;
      const user = new User();
      user.email = data.email;
      user.name = data.name;
      user.token = data.token;
      user.authorities = [data.role];
      user._id = data.userId;
      this.authenticationService.saveUserInSession(user);

      this.router.navigate(['/home']).then(
        () => {
          this.toastr.success('Login com sucesso!', 'Success');
        }
      );
    } else {
      this.error = data.message;
    }
  }
  
  recover() {
    this.router.navigate(['/recover']);
  }

  signupUser() {
    this.router.navigate(['/signup']);
  }
}
