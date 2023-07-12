import { UserHome } from './home/home.component';
import { UpdateAccount } from './update-account/updateaccount.component';
import { UploadPDFsReports } from './upload-pdfs-reports/upload-pdfs-reports.component';
import { MenuComponent } from '../_components/menu/menu.component';
import { Reports } from './dashboard/reports.component';
import { BrokerageNotes } from './brokerage-notes/brokerage-notes.component';
import { ModalDialog } from '../_components/modal/modal.component';
import { BarChartComponent } from '../_components/bar-chart/bar-chart.component';
import { OperationsComponent } from './operations/operations.component';

import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgxPaginationModule } from 'ngx-pagination';
import { TableChart } from '../_components/table/table-chart.component';
import { ExtractionErrorComponent } from './brokerage-notes/extraction-errors/extraction-errors.component';

@NgModule({
  declarations: [
    UserHome,
    UpdateAccount,
    UploadPDFsReports,
    Reports,
    BrokerageNotes,
    MenuComponent,
    ModalDialog,
    BarChartComponent,
    TableChart,
    OperationsComponent,
    ExtractionErrorComponent
    ],
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    NgxPaginationModule,
    NgbModule
  ]
})
export class UserHomeModule { }
