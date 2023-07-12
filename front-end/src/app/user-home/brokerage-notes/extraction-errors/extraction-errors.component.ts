import { AuthenticationService } from "src/app/_service/authentication.service";
import { Component, OnInit } from '@angular/core';
import { Title } from "@angular/platform-browser";
import { Router } from "@angular/router";
import { ExtractionErrorService } from "src/app/_service/extraction-error.service";

@Component({
    selector: "extraction-errors",
    templateUrl: "./extraction-errors.component.html",
})
export class ExtractionErrorComponent implements OnInit {

    user = null
    now = new Date()
    loading = false

    extractionErrors = []

    errors = []

    constructor(
        private authenticationService: AuthenticationService,
        private titleService: Title,
        private router: Router,
        private extractionErrorService: ExtractionErrorService
    ) {
        this.titleService.setTitle("Detalhe da nota de corretagem")

        if (this.authenticationService.currentUserValue == null){
            this.authenticationService.logout()
            this.router.navigate(['/'])
        }
    }

    ngOnInit(): void {
        this.user = this.authenticationService.currentUserValue
        this.retrieveExtractionErrors()
    }

    retrieveExtractionErrors(){
        this.extractionErrorService.getByFileId(history.state.fileId).subscribe({
            next: (resp) => {
                this.extractionErrors = resp.errors
            },
            error: (err) => {
                this.errors.push(err.message)
            }
        })
    }
}