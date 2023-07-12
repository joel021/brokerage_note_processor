import { NgModule } from '@angular/core'
import { RouterModule, Routes } from '@angular/router'
import { Confirmation } from './public/confirmation/confirmation.component'
import { Recover } from './public/recover/recover.component'
import { Signin } from './public/signin/signin.component'
import { Signup } from './public/signup/signup.component'
import { PublicHome } from './public/home/home.component'
import { BrokerageNotes } from './user-home/brokerage-notes/brokerage-notes.component'
import { UserHome } from './user-home/home/home.component'
import { Reports } from './user-home/dashboard/reports.component'
import { UploadPDFsReports } from './user-home/upload-pdfs-reports/upload-pdfs-reports.component'
import { UpdateAccount } from './user-home/update-account/updateaccount.component'
import { OperationsComponent } from './user-home/operations/operations.component'
import { ExtractionErrorComponent } from './user-home/brokerage-notes/extraction-errors/extraction-errors.component'
import { PrivacyPolicy } from './public/privacy-policy/privacy-policy.component'

const routes: Routes = [
  {path: "privacy-policy", component: PrivacyPolicy},
  {path: "public-home", component: PublicHome},
  {path: "recover", component: Recover},
  {path: "confirmation", component: Confirmation},
  {path: "send-brokerage-notes", component: UploadPDFsReports},
  {path: "reports", component: Reports },
  {path: "brokerage-notes", component: BrokerageNotes},
  {path: "account", component: UpdateAccount},
  {path: "operations", component: OperationsComponent},
  {path: "brokerage-notes/extraction-errors", component: ExtractionErrorComponent},
  {
    path: "signup",
    component: Signup
  },
  {
    path: "signin",
    component: Signin
  },
  {
    path: "home",
    component: UserHome
  },
  {
    path: "**", redirectTo: "public-home"
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: "reload" })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
