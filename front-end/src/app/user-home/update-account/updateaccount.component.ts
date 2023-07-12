import { Component, OnInit, Inject, ElementRef } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { AuthenticationService } from 'src/app/_service/authentication.service';
import { throwError } from 'rxjs';
import { UserService } from 'src/app/_service/user.service';

import { Router } from '@angular/router';

@Component({
  selector: 'update-account',
  templateUrl: './updateaccount.component.html',
  styleUrls: ['./updateaccount.component.css']
})
export class UpdateAccount implements OnInit {

  userForm: FormGroup;
  loading = false;
  submitted = false;
  returnUrl: string;
  isLoggedin?: boolean;
  error = '';
  user = this.authenticationService.currentUserValue;
  message = ""

  constructor(
    @Inject(DOCUMENT) public document,
    public elementRef: ElementRef,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private authenticationService: AuthenticationService,
    private userService: UserService,
    private router: Router
  ) {
    if (this.authenticationService.currentUserValue == null) {
      this.authenticationService.logout()
      this.router.navigate(['/signin'])
    }
  }

  ngOnInit() {
    this.userForm = this.formBuilder.group(
      {
        name: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        updatePass: ['', Validators.required],
        password: ['',],
        passwordConfirmation: ['', ]
      }
    );

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    this.userForm.patchValue({
      email: this.user.email,
      name: this.user.name
    });
  }

  get controls() { return this.userForm.controls; }

  onSubmit() {
    this.submitted = true;

    if (this.userForm.invalid) {

      if (!this.controls.email.value) {
        return;
      }
      if (!this.controls.name.value) {
        return;
      }

      if (this.controls.updatePass.value && this.controls.password.value != this.controls.passwordConfirmation.value){
        return;
      }

    }

    this.loading = true;

    var user = {
      userId: this.user._id,
      email: this.controls.email.value,
      password: this.controls.password.value,
      passwordConfirmation: this.controls.passwordConfirmation.value,
      name: this.controls.name.value
    }

    this.userService.update(user)
      .subscribe({
        next: respObject => {
          this.loading = false;

          this.message = "Salvo com sucesso. A atualização surtirá efeito qundo você fazer login novamente."
        },
        error: respError => {
          this.message = "Não foi possível salvar."
          this.loading = false;
          if (respError) {
            this.error = respError.message;
            return throwError(() => respError.message);
          } else {
            return throwError(() => respError);
          }
        }
      })

  }
}
