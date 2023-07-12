import { AuthenticationService } from "src/app/_service/authentication.service";
import { PDFFilesService } from "src/app/_service/pdf-file.service";
import { OperationService } from "src/app/_service/operation.service";
import { ExtractionErrorService } from "src/app/_service/extraction-error.service";
import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr'
import { Title } from "@angular/platform-browser";
import { Activity } from "src/app/_model/activity.interface";
import { Router } from "@angular/router";

@Component({
    selector: "brokerage-notes",
    templateUrl: "./brokerage-notes.component.html",
    styleUrls: ['./brokerage-notes.component.css']
})
export class BrokerageNotes implements OnInit, Activity {

    user = null
    brokerageFiles = []
    filesEdited = null
    page = 0
    pageQuantity = 10
    quantity:number
    pagesQuantity:number
    hasNext = true
    now = new Date()
    loading = false

    errors = []
    completed = false
    
    extractionErrorsQuantity = {}

    constructor(
        private toastr: ToastrService,
        private authenticationService: AuthenticationService,
        private pdfFileService: PDFFilesService,
        private operationService: OperationService,
        private extractionErrorService: ExtractionErrorService,
        private titleService: Title,
        private router: Router
    ) {
        this.titleService.setTitle("Notas de corretagem")

        if (this.authenticationService.currentUserValue == null){
            this.authenticationService.logout();
            this.router.navigate(['/signin'])
        }
    }

    ngOnInit(): void {
        this.user = this.authenticationService.currentUserValue
        this.retriveFiles()
        this.getQuantity()
        this.hasNext = true
    }

    retriveFiles() {
        this.pdfFileService.getFiles(this.page, this.pageQuantity).subscribe({
            next: (event) => {
                if (event.files.length > 0) {
                    this.brokerageFiles = event.files
                } else {
                    this.hasNext = false
                }
                this.pagesQuantity = Math.ceil(this.quantity / this.pageQuantity) - 1

                this.retrieveExtractionErrorsQuantity()
            },
            error: (err) => {
                console.log(err);
                this.toastr.error('Não foi possível obter suas notas de corretagem.', 'Error');
            }
        })
    }

    retrieveExtractionErrorsQuantity(){

        for(var i = 0; i < this.brokerageFiles.length; i++){
            this.extractionErrorService.getCountByFileId(this.brokerageFiles[i].fileId).subscribe({
                next: (result) => {
                    this.extractionErrorsQuantity[result.fileId] = result.count
                },
                error: (error) => {
                }
            })
        }
    }

    getQuantity(){
        this.pdfFileService.count().subscribe({
            next: (respObject) => {
                this.quantity = respObject.quantity
                this.pagesQuantity = Math.ceil(this.quantity / this.pageQuantity) - 1
            },
            error: (err) => {
                this.errors.push("Não foi possível obter a quantidade de notas de corretagem.")
            }
        })
    }

    prevPage() {

        console.log("prev page: "+this.hasNext+", "+this.page)
        if (this.page > 1) {
            this.page -= 1
            this.hasNext = true
        } else {
            this.page = 0
        }

        this.retriveFiles()
    }

    nextPage() {

        if (this.hasNext && this.page < this.pagesQuantity) {
            this.page += 1
        }
        this.retriveFiles()
    }

    updateQuantity() {
        var quantity = parseInt((<HTMLInputElement>document.getElementById("page_quatity")).value)
        if (quantity >= 0) {
            this.pageQuantity = quantity
            this.retriveFiles()
        }
    }

    difDate(current: string) {
        return Math.ceil(this.now.getTime() - new Date(current + "").getTime()) / (1000 * 60 * 60 * 24)
    }

    getDate(date:string){
        
        var dateSplitTimeArr = date.split("T")
        var dateArr = dateSplitTimeArr[0].split("-")
        var timeArr = dateSplitTimeArr[1].split(":")

        return {
            year: dateArr[0],
            month: dateArr[1],
            day: dateArr[2],
            hours: timeArr[0],
            minutes: timeArr[1]
        }
    }

    sendFileToProcess(idx: number) {
        var oldUpdatedAt = this.brokerageFiles[idx].updatedAt
        this.brokerageFiles[idx].updatedAt = new Date()
        
        this.loading = true
        this.pdfFileService.saveInfo(this.brokerageFiles[idx]).subscribe({
            next: (result) => {
                this.loading = false
                this.toastr.success("Recarregado com sucesso. Espere enquanto ele é processado.", "Success")
            },
            error: (err) => {
                this.loading = false
                this.brokerageFiles[idx].updatedAt = oldUpdatedAt
                this.toastr.error("Não foi possível recarregar. " + err.errors[0])
            }
        })
    }

    callBackComponent(arg) {
        if (arg['method'] == "deleteFile") {
            this.deleteFile(arg.arg)
        } else if (arg['method'] == "deleteOperations") {
            this.deleteOperations(arg.arg)
        }
    }
    deleteFile(idx) {

        var fileInfo = this.brokerageFiles[idx]
        this.loading = true

        this.pdfFileService.deleteFile(fileInfo.fileId).subscribe({
            next: (resp) => {
                this.brokerageFiles[idx].deletedAt = new Date()
                this.toastr.success("Deletado com sucesso.", "Success")
                this.loading = false
            },
            error: (err) => {
                this.loading = false

                this.toastr.error("Não foi possível deletar. " + err.errors[0])
            }
        })
    }

    deleteOperations(idx) {

        this.loading = true
        this.operationService.deleteOperationsByFileId(this.brokerageFiles[idx].fileId).subscribe({
            next: (resp) => {
                this.loading = false
                this.toastr.success(resp.message, "Success")
            },
            error: (err) => {
                this.loading = false

                if (err.errors)
                    this.toastr.error(err.errors[0], "Error")
                
                if (err.error)
                    this.toastr.error(err.error, "Error")
            }
        })
    }

    onEdit(idx: number) {

        if (this.filesEdited == null && !this.brokerageFiles[idx].deletedAt) {
            this.filesEdited = {}
        }
        
        var fileInfo = { 
            idx: idx,
            fileId: this.brokerageFiles[idx].fileId,
            name: this.brokerageFiles[idx].name,
            password: (<HTMLInputElement>document.getElementById("password_" + idx)).value,
            stockBroker: (<HTMLInputElement>document.getElementById("stockBroker_" + idx)).value,
            deletedAt: this.brokerageFiles[idx].deletedAt,
            extractedAt: this.brokerageFiles[idx].extractedAt
        }
        this.filesEdited["f_" + idx] = fileInfo
    }

    saveEditions() {

        if (this.filesEdited == null){
            return
        }

        for (const key in this.filesEdited) {

            var idx = this.filesEdited[key].idx

            var fileInfo = { idx: idx,
                fileId: this.filesEdited[key].fileId,
                name: this.filesEdited[key].name,
                password: this.filesEdited[key].password,
                stockBroker: this.filesEdited[key].stockBroker,
                deletedAt: this.filesEdited[key].deletedAt,
                extractedAt: this.filesEdited[key].extractedAt
            }
            delete this.filesEdited[key]
            this.updateFileInfo(fileInfo)
        }
    }

    updateFileInfo(newFileInfo) {

        this.loading = true
        this.pdfFileService.saveInfo(newFileInfo).subscribe({
            next: (result) => {
                this.brokerageFiles[newFileInfo.idx] = newFileInfo
                this.loading = false

                if (Object.keys(this.filesEdited).length === 0){
                    this.completed = true
                }
            },
            error: (err) => {
                if (Object.keys(this.filesEdited).length === 0){
                    this.completed = true
                }

                this.loading = false
                this.errors.push(err.errors[0])
            }
        })
    }

    errorDetails(fileId:string){
        this.router.navigate(["brokerage-notes/extraction-errors"], {
            state: {
                fileId: fileId
            }
        })
    }

}