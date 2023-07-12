import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Operation } from '../_model/operation.model';

@Injectable({
  providedIn: 'root'
})
export class OperationService {

  constructor(private http: HttpClient) { 

  }

  deleteOperationsByFileId(fileId:string): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/users/operations/by_file/${fileId}`)
  }

  overallProfitByMonth(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/users/operations/overall_profit_by_month`)
  }

  profitMonthTypeOpTypeMarket(typeOp:string, typeMarket:string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/users/operations/profit_month_typeop_typemarket?typeOp=${typeOp}&typeMarket=${typeMarket}`)
  }

  profitByActive(): Observable<any>{
    return this.http.get(`${environment.apiUrl}/users/operations/profit_per_active`)
  }

  getOperations(page:number, quantity:number): Observable<any>{
    return this.http.get(`${environment.apiUrl}/users/operations/?page=${page}&quantity=${quantity}`)
  }

  count(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/users/operations/count`)
  }

  save(operation: Operation):Observable<any> {
    return this.http.post(`${environment.apiUrl}/users/operations/`, operation)
  }
  update(operation) : Observable<any> {
    return this.http.patch(`${environment.apiUrl}/users/operations/${operation.operationId}`, operation)
  }

  delete(operationId) : Observable<any> {
    return this.http.delete(`${environment.apiUrl}/users/operations/${operationId}`)
  }

}