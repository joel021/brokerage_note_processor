import { Component, OnInit, Inject, ElementRef } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { catchError } from 'rxjs/operators';
import { AuthenticationService } from 'src/app/_service/authentication.service';
import { throwError } from 'rxjs';
import { User } from 'src/app/_model/user';

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.css']
})
export class Confirmation implements OnInit {

  confirmationForm: FormGroup;
  loading = false;
  submitted = false;
  returnUrl: string;
  isLoggedin?: boolean;
  error = '';

  constructor(
    @Inject(DOCUMENT) public document,
    public elementRef: ElementRef,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authenticationService: AuthenticationService
  ) {

  }

  ngOnInit() {
    this.confirmationForm = this.formBuilder.group({
      code: ['', Validators.required]
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  get controls() { return this.confirmationForm.controls; }

  onSubmit() {
    this.submitted = true;

    if (this.confirmationForm.invalid) {
      return;
    }

    this.loading = true;

    var userInfo = this.authenticationService.getUserInfo();

    if (userInfo != null) {

      this.authenticationService.confirmAccount(userInfo.email, this.controls.code.value)
        .pipe(catchError(respError => {

          this.error = respError.message;
          this.loading = false;
          return throwError(() => respError.message);
        }))
        .subscribe(data => {
          
          this.loading = false;
          if (data.token != null) {
            console.log(data);
            const user = new User();
            user.name = data.name;
            user.email = data.email;
            user.token = data.token;
            user.authorities = [data.role];
            user._id = data.userId;
            this.authenticationService.saveUserInSession(user);
            this.authenticationService.saveUserInfo(null);
            this.router.navigate(['/users/update-account'])
          } else {
            this.error = data.message;
          }
        }

        );
    }else{
      this.error = "Por favor, forneça o seu e-mail.";
      this.router.navigate(['/recover']);
    }

  }

  back() {
    this.router.navigate(['/recover']);
  }

}
