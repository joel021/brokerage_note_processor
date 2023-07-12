import { NgModule } from '@angular/core'
import { ReactiveFormsModule } from '@angular/forms'
import { CommonModule } from '@angular/common'
import { RouterModule } from '@angular/router'

import { Signup } from './signup/signup.component'
import { Signin } from './signin/signin.component'
import { Recover } from './recover/recover.component'
import { PrivacyPolicy } from './privacy-policy/privacy-policy.component'
import { PageActionsComponent } from '../_components/page-actions/page-actions.component'
import { Confirmation } from './confirmation/confirmation.component'
import { PublicHome } from './home/home.component'

import { NgbModule } from '@ng-bootstrap/ng-bootstrap'


@NgModule({
  declarations: [
    Signup,
    Signin,
    Recover,
    Confirmation,
    PageActionsComponent,
    PublicHome,
    PrivacyPolicy
  ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    NgbModule
  ]
})
export class PublicModule { }
