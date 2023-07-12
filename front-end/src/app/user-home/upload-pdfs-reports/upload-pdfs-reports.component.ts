import { AuthenticationService } from 'src/app/_service/authentication.service';
import { PDFFilesService } from 'src/app/_service/pdf-file.service'

import {Title} from "@angular/platform-browser";
import { Component, OnInit, Inject, ElementRef } from '@angular/core'
import { DOCUMENT } from '@angular/common'
import { ToastrService } from 'ngx-toastr';
import { FormBuilder, FormGroup} from '@angular/forms';
import { Router } from '@angular/router';
import { PdfFile } from 'src/app/_model/pdf.file.model';


@Component({
  selector: 'upload-pdfs-reports',
  templateUrl: './upload-pdfs-reports.component.html',
  styleUrls: ['./upload-pdfs-reports.component.css']
})
export class UploadPDFsReports implements OnInit {
  returnUrl: string
  isLoggedin?: boolean
  error = ''
  user = this.authenticationService.currentUserValue
  errors = []
  filesInfos = []
  selectedFiles
  acceptedFiles = "application/pdf"
  filesInfosForm: FormGroup
  loading = false
  completed = false

  constructor(
    @Inject(DOCUMENT) public document,
    public elementRef: ElementRef,
    private authenticationService: AuthenticationService,
    private titleService: Title,
    private formBuilder: FormBuilder,
    private toastr: ToastrService,
    private uploadFilesService: PDFFilesService,
    private router: Router
  ) {

    if (this.authenticationService.currentUserValue == null) {
      this.authenticationService.logout();
      this.router.navigate(['/sigin'])
    }

    this.titleService.setTitle("Enviar notas de corregem")
    this.filesInfosForm = this.formBuilder.group({
      files: this.formBuilder.array([])
    })
  }

  ngOnInit() {
    if (this.authenticationService.currentUserValue == null){
      this.authenticationService.logout()
      this.router.navigate(['/signin'])
    }
  }

  setRepeat(idx){
    this.filesInfos[idx].repeat = true
  }

  repeatValue(attribute:string, idx:number){
    var value = (<HTMLInputElement>document.getElementById(attribute+"_"+idx)).value
    for(var i = idx+1; i < this.filesInfos.length; i++){
      (<HTMLInputElement>document.getElementById(attribute+"_"+i)).value = value;
    }
  }

  
  selectFiles(event) {

    this.completed = false
    this.selectedFiles = event.target.files

    this.errors = []
  
    for (let i = 0; i < this.selectedFiles.length; i++) {
      this.filesInfos[i] = { idx: i, perComplete: 0, name: this.selectedFiles[i].name, error: false, fileId:null, 
        stockBroker:null, password: null }
    }

  }

  saveFiles(){

    this.errors = []
    this.loading = true
  
    for (let i = 0; i < this.filesInfos.length; i++) {

      this.filesInfos[i].password = (<HTMLInputElement>document.getElementById("password_"+i)).value
      this.filesInfos[i].stockBroker = (<HTMLInputElement>document.getElementById("stockBroker_"+i)).value
      this.upload(i, this.selectedFiles[this.filesInfos[i].idx])
    }
  }

  upload(idx, file) {
    
    this.uploadFilesService.upload(file, new PdfFile(this.filesInfos[idx].stockBroker, this.filesInfos[idx].password)).subscribe(
      {
        next: (event) => {

          if (event.loaded != null){
            this.filesInfos[idx].perCompleted = Math.round(100 * event.loaded / event.total)
          }

          if (event.fileId != null){
            this.filesInfos[idx].fileId = event.fileId
            this.filesInfos[idx].perCompleted = 100
          }

          if (idx == this.filesInfos.length - 1){
            this.loading = false
            this.toastr.success('Todas as informações foram salvas com sucesso!', 'Success')
            this.completed = true
          }

        },
        error: (err) => {

          console.log(err)
          if (idx == this.filesInfos.length - 1){
            this.loading = false
            this.completed = true
          }

          this.filesInfos[idx].perCompleted = 0

          if (err.errors){
            this.errors.push('Não foi possível enviar o arquivo ' + file.name + ". "+err.errors[0])
          }else{
            this.errors.push('Não foi possível enviar o arquivo ' + file.name + ". ")
          }
          
          this.filesInfos[idx].error = true

          if (idx == this.filesInfos.length - 1){
            this.loading = false
            this.toastr.success('Todas as informações foram salvas com sucesso!', 'Success')
            this.completed = true
          }
        }
      }
    )
    
  }

}
