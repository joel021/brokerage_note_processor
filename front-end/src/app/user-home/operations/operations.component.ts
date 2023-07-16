import { AuthenticationService } from 'src/app/_service/authentication.service';
import { OperationService } from 'src/app/_service/operation.service';
import { DAYTRADE, SWINGTRADE, CASH_MARKET, OPTION_MARKET, FUTURE_MARKET, BOUGHT, SOLD, CLOSED, ACTIVE, OPTION } from 'src/app/_constants/market';
import { Title } from "@angular/platform-browser";
import { Component, OnInit, Inject, ElementRef } from '@angular/core'
import { DOCUMENT } from '@angular/common'
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router'
import { Activity } from 'src/app/_model/activity.interface';
import { Operation } from 'src/app/_model/operation.model';
import { saveAs } from 'file-saver';

@Component({
  selector: 'operations-component',
  templateUrl: './operations.component.html',
  styleUrls: ['./operations.component.css']
})
export class OperationsComponent implements OnInit, Activity {
  returnUrl: string
  isLoggedin?: boolean
  error = ''
  user = this.authenticationService.currentUserValue
  errors = []
  loading = false

  DAYTRADE = DAYTRADE
  SWINGTRADE = SWINGTRADE
  CASH_MARKET = CASH_MARKET
  OPTION_MARKET = OPTION_MARKET
  FUTURE_MARKET = FUTURE_MARKET
  BOUGHT = BOUGHT
  SOLD = SOLD
  CLOSED = CLOSED
  ACTIVE = ACTIVE
  OPTION = OPTION

  page = 0
  quantity = 1
  hasNext = true
  pagesQuantity = 0
  pageQuantity = 10

  operations = []

  edited = false
  checkedQtd = 0

  constructor(
    @Inject(DOCUMENT) public document,
    public elementRef: ElementRef,
    private authenticationService: AuthenticationService,
    private titleService: Title,
    private operationService: OperationService,
    private toastService: ToastrService,
    private router: Router
  ) {
    this.titleService.setTitle("Operações realizadas")

    if (this.authenticationService.currentUserValue == null) {
      this.authenticationService.logout()
      this.router.navigate(['/signin'])
    }
  }

  ngOnInit() {
    if (this.authenticationService.currentUserValue == null) {
      this.authenticationService.logout();
      this.router.navigate(['/'])
    }

    this.retrieveOperations()
  }

  callBackComponent(args) {

    this.deleteChecked()

  }

  retrieveOperations() {

    this.operationService.count().subscribe({
      next: (respObject) => {
        this.quantity = respObject.quantity

        this.operationService.getOperations(this.page, this.pageQuantity).subscribe({
          next: (resp) => {

            this.operations = resp.operations

            if (resp.operations.length > 0) {
              this.pagesQuantity = Math.ceil(this.quantity / this.pageQuantity) - 1
            } else {
              this.hasNext = false
            }
          },
          error: (err) => {
            this.errors.push("Não foi possível obter as operações." + err.error)
          }
        })

      },
      error: (err) => {
        this.errors.push("Não foi possível obter a quantidade de operações.")
      }
    })


  }

  prevPage() {

    if (this.page > 1) {
      this.page -= 1
      this.hasNext = true

      this.retrieveOperations()
    } else {
      this.page = 0
    }

  }

  nextPage() {

    if (this.hasNext && this.page < this.pagesQuantity) {
      this.page += 1
      this.retrieveOperations()
    }
  }

  updateQuantity() {
    var quantity = parseInt((<HTMLInputElement>document.getElementById("page_quatity")).value)
    if (quantity >= 0) {
      this.pageQuantity = quantity
      this.retrieveOperations()
    }
  }

  editAttribute(attribute: string, idx: number) {
    this.edited = true

    this.operations[idx][attribute] = (<HTMLInputElement>document.getElementById(attribute + "_" + idx)).value
    this.operations[idx].edited = true
  }

  saveEditions() {
    this.errors = []
    for (var i = 0; i < this.operations.length; i++) {

      if (this.operations[i].edited) {
        this.saveEdition(i)
      }
    }
    this.edited = false
  }

  closeMonthShape(closeMonth: string) {

    if (closeMonth == null || closeMonth.length == 0) {
      return null
    }

    var yearMonth = closeMonth.split("-")

    if (yearMonth[0].length != 4 || yearMonth[1].length != 2) {
      this.errors.push("O mês de fechamento deve ser na forma <ANO>-<MÊS>. Formato esperado: yyyy-mm\n")
      throw new Error('O mês de fechamento deve ser na forma <ANO>-<MÊS>. Formato esperado: yyyy-mm');
    }

    var year = parseInt(yearMonth[0])
    var month = parseInt(yearMonth[1])

    if (year < 2000 || year > 2100) {
      this.errors.push("O ano fornecido não é válido.\n")
      throw new Error()
    }

    if (month > 12 || month < 1) {
      this.errors.push("O mês fornecido não é válido.\n")
      throw new Error()
    }

    return closeMonth
  }

  saveEdition(i: number) {
    this.loading = true

    if (this.operations[i].id == null) {
      var operation = new Operation()

      try {
        operation.closeMonth = this.closeMonthShape(this.operations[i].closeMonth)
      } catch (error) {
        this.loading = false
        return
      }

      operation.date = this.operations[i].date
      operation.name = this.operations[i].name.toUpperCase()
      operation.value = this.operations[i].value
      operation.qtd = this.operations[i].qtd
      operation.typeOp = (<HTMLInputElement>document.getElementById("typeOp_" + i)).value
      operation.type = (<HTMLInputElement>document.getElementById("type_" + i)).value
      operation.wallet = (<HTMLInputElement>document.getElementById("wallet_" + i)).value
      operation.typeMarket = (<HTMLInputElement>document.getElementById("typeMarket_" + i)).value

      this.operationService.save(operation).subscribe({
        next: (result) => {
          this.operations[i].id = result.id
          this.operations[i].editedSuccess = true
          this.loading = false
        },
        error: (err) => {
          this.loading = false

          if (err.errors) {
            for (var k = 0; k < err.errors.length; k++) {
              this.errors.push(err.errors[k])
            }
          } else {
            this.errors.push("Não foi possível adicionar a operação\n")
            this.operations[i].error = true
          }

        }
      })

    } else {
      this.operationService.update(this.operations[i]).subscribe({
        next: (resp) => {
          this.loading = false

          this.operations[i].editedSuccess = true
        },
        error: (error) => {
          this.operations[i].error = true
          this.loading = false
          this.errors.push(error)
        }
      })
    }

  }

  setCheckedToAll() {
    var checked = (<HTMLInputElement>document.getElementById("check_all")).checked

    for (var i = 0; i < this.operations.length; i++) {
      this.operations[i].checked = checked
    }

    if (checked) {
      this.checkedQtd = this.operations.length
    } else {
      this.checkedQtd = 0
    }

  }

  setChecked(idx: number) {
    var checked = (<HTMLInputElement>document.getElementById("check_" + idx)).checked
    if (checked) {
      this.checkedQtd += 1
    } else {
      this.checkedQtd -= 1
    }

    this.operations[idx].checked = checked
  }

  deleteChecked() {
    this.errors = []

    for (var i = 0; i < this.operations.length; i++) {

      if (this.operations[i].checked) {
        this.deleteOperation(i)
      }

    }
  }

  deleteOperation(idx: number) {

    this.loading = true
    this.operationService.delete(this.operations[idx].id).subscribe({
      next: (resp) => {
        this.loading = false
        this.operations[idx].checked = false
        this.operations[idx].deleted = true
        this.checkedQtd -= 1

        if (this.checkedQtd == 0) {
          this.toastService.success(resp.message, "Success")
          this.retrieveOperations()
        }
      },
      error: (err) => {
        this.loading = false
        this.errors.push(err)

        if (idx == this.operations.length) {
          this.retrieveOperations()
        }
      }
    })
  }

  addOperation() {
    this.operations.unshift({ value: 0, qtd: 0 })
  }
  cancelAddOperation(idx: number) {
    this.operations.splice(idx, 1)
  }

  downloadCsv(){
    this.operationService.downloadCsv().subscribe({
      next: (res) => {
        console.log(res)
        saveAs(res, "operations-"+this.user._id+".csv")
      }
    })
  }

}
