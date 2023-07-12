import { Component, OnInit, Inject, ElementRef } from '@angular/core'
import { DOCUMENT } from '@angular/common'
import { Router, ActivatedRoute } from '@angular/router'
import { FormBuilder, FormGroup, Validators } from '@angular/forms'

import { catchError } from 'rxjs/operators'
import { AuthenticationService } from 'src/app/_service/authentication.service'
import { throwError } from 'rxjs'

@Component({
  selector: 'app-recover',
  templateUrl: './recover.component.html',
  styleUrls: ['./recover.component.css']
})
export class Recover implements OnInit {

  recoverForm: FormGroup
  loading = false
  submitted = false
  returnUrl: string
  isLoggedin?: boolean
  error = ''

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
    this.recoverForm = this.formBuilder.group({
      email: ['', Validators.required]
    })

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/'
  }

  get controls() { return this.recoverForm.controls }

  onSubmit() {
    this.submitted = true

    if (this.recoverForm.invalid) {
      return
    }

    this.authenticationService.verificationCode(this.controls.email.value)
    .pipe(catchError(respError => {
      this.error = respError.message
      this.loading = false
      return throwError(() => respError.message)
    }))
    .subscribe(data => {

        this.authenticationService.saveUserInfo({"email":this.controls.email.value})
        this.loading = false
        this.router.navigate(['/confirmation'])
      }
      
    )
    
  }

  signupUser() {
    this.router.navigate(['/signup'])
  }

  signinUser() {
    this.router.navigate(['/signin'])
  }
}
