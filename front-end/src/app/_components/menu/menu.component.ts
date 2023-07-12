import { Component, OnInit, Inject } from '@angular/core'
import { DOCUMENT } from '@angular/common'
import { AuthenticationService } from 'src/app/_service/authentication.service'

@Component({
  selector: 'left-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {

  loading = false
  message = null
  user = this.authenticationService.currentUserValue

  displayModal = "none"
  menu = "home"

  mainNavBar = false

  constructor(
    @Inject(DOCUMENT) public document,
    private authenticationService: AuthenticationService
  ) {

  }

  ngOnInit() {

  }

  troggleNavMenu() {

    if (this.mainNavBar) {
      (<HTMLInputElement>document.getElementById("mainNavBar")).setAttribute("style", "display:none")
      this.mainNavBar = false
    } else {
      (<HTMLInputElement>document.getElementById("mainNavBar")).setAttribute("style", "display:block")
      this.mainNavBar = true
    }
  }

  logout() {
    this.authenticationService.logout()
  }

}