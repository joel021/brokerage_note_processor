import { Active } from 'src/app/_model/active.model';
import { AuthenticationService } from 'src/app/_service/authentication.service';
import { OperationService } from 'src/app/_service/operation.service';
import ChartModel from 'src/app/_model/chart.model';
import { CASH_MARKET, OPTION_MARKET, FUTURE_MARKET, SWINGTRADE, DAYTRADE } from 'src/app/_constants/market';
import { Activity } from 'src/app/_model/activity.interface';

import { Component, OnInit } from '@angular/core';
import { Title } from "@angular/platform-browser";
import { Router } from "@angular/router"
import { HTTP_STATUS_UNAUTHORIZED } from 'src/app/_constants/http.constants';

@Component({
  selector: 'reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class Reports implements OnInit, Activity {

  profitByMonthChart: ChartModel = new ChartModel(
    "Total - Lucro/Prejuízo por mês",
    ["(R$)"],
    "profitByMonth"
  );

  swingCashProfitByMonthChart: ChartModel = new ChartModel(
    "Ações - Lucro/Prejuízo por mês",
    ["(R$)"],
    "swingCashProfitByMonth"
  );

  swingOptionProfitByMonthChart: ChartModel = new ChartModel(
    "Opções - Lucro/Prejuízo por mês",
    ["(R$)"],
    "swingOptionProfitByMonth"
  );

  dayCashProfitByMonthChart: ChartModel = new ChartModel(
    "Day trade à Vista - Lucro/Prejuízo por mês",
    ["(R$)"],
    "dayCashProfitByMonth"
  )

  dayFutureProfitByMonthChart: ChartModel = new ChartModel(
    "Day trade Futuro - Lucro/Prejuízo por mês",
    ["(R$)"],
    "dayFutureProfitByMonth"
  )

  profitByActiveChart: ChartModel = new ChartModel("Lucro por ativo", [
    "Ativo",
    "Lucro/Prejízo (R$)",
  ]);

  activesService = new Active()

  constructor(
    private authenticationService: AuthenticationService,
    private titleService: Title,
    private operationService: OperationService,
    private router: Router
  ) {

    if (this.authenticationService.currentUserValue == null) {
      this.authenticationService.logout()
      this.router.navigate(['/signin'])
    }

    this.titleService.setTitle("Dashboard - Relatórios")
  }

  ngOnInit() {
    this.getOverAllProfitByMonth()
    this.getSwingCashProfitByMonthChart()
    this.getSwingOptionProfitByMonthChart()
    this.getDayCashProfitByMonthChart()
    this.getProfitByActive()
    this.getDayFutureProfitByMonthChart()
  }

  getOverAllProfitByMonth() {
    this.operationService.overallProfitByMonth().subscribe({
      next: (resp) => {
        if (resp.length > 0){
          this.profitByMonthChart.data = resp
        }
      },
      error: (err) => {
        this.onRetrieveError(err)
      }
    })
  }

  getSwingCashProfitByMonthChart() {
    this.operationService.profitMonthTypeOpTypeMarket(SWINGTRADE, CASH_MARKET).subscribe({
      next: (resp) => {
        if (resp.length > 0){
          this.swingCashProfitByMonthChart.data = resp
        }
      },
      error: (err) => {
        this.onRetrieveError(err)
      }
    })
  }

  getSwingOptionProfitByMonthChart() {
    this.operationService.profitMonthTypeOpTypeMarket(SWINGTRADE, OPTION_MARKET).subscribe({
      next: (resp) => {

        if (resp.length > 0){
          this.swingOptionProfitByMonthChart.data = resp
        }
      },
      error: (err) => {
        this.onRetrieveError(err)
      }
    })
  }

  getDayCashProfitByMonthChart() {
    this.operationService.profitMonthTypeOpTypeMarket(DAYTRADE, CASH_MARKET).subscribe({
      next: (resp) => {
        if (resp.length > 0){
          this.dayCashProfitByMonthChart.data = resp
        }
      },
      error: (err) => {
        this.onRetrieveError(err)
      }
    })
  }

  getDayFutureProfitByMonthChart() {
    this.operationService.profitMonthTypeOpTypeMarket(DAYTRADE, FUTURE_MARKET).subscribe({
      next: (resp) => {
        if (resp.length > 0){
          this.dayFutureProfitByMonthChart.data = resp
        }
      },
      error: (err) => {

        this.onRetrieveError(err)
      }
    })
  }

  getProfitByActive() {
    this.operationService.profitByActive().subscribe({
      next: (resp) => {
        this.activesService.parseNamesToActiveNames(resp)
        var profitByActive = Object()

        for (var i = 0; i < resp.length; i++) {

          if (profitByActive[resp[i][0]] == undefined) {
            profitByActive[resp[i][0]] = 0
          }

          profitByActive[resp[i][0]] += resp[i][1]
        }
        var profitByActiveArr = []

        for (var key in profitByActive) {
          profitByActiveArr.push([key, profitByActive[key]])
        }

        this.profitByActiveChart.data = profitByActiveArr
      },
      error: (err) => {
        this.onRetrieveError(err)
      }
    })
  }

  callBackComponent(args) {

    if (args.activeFilter != null) {
      console.log("Not implemented! Filter by active: " + args.activeFilter)
    }
  }

  onRetrieveError(err){
    if (err.status == HTTP_STATUS_UNAUTHORIZED){
      this.authenticationService.logout()
      this.router.navigate(['/signin'])
    }
  }
}
