import { Component, OnInit, Inject, ElementRef } from '@angular/core'
import {Title} from "@angular/platform-browser"
import { DOCUMENT } from '@angular/common'
import { environment } from 'src/environments/environment'

@Component({
  selector: 'privacy-policy',
  templateUrl: './privacy-policy.component.html'
})
export class PrivacyPolicy implements OnInit {

  adminEmail = environment.contactEmail
  externalUrl = environment.externalURL
  
  constructor(
    @Inject(DOCUMENT) public document,
    public elementRef: ElementRef,
    private titleService:Title
  ) {
    this.titleService.setTitle("Termos de privacidade e contato")
  }

  ngOnInit() {

  }
}
