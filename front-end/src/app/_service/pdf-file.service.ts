import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { PdfFile } from '../_model/pdf.file.model';

@Injectable({
  providedIn: 'root'
})
export class PDFFilesService {

  constructor(private http: HttpClient) { 

  }

  upload(file: File, fileInfo: PdfFile): Observable<any> {
    const formData: FormData = new FormData();

    formData.append('file', file);
    formData.append
    return this.http.post(`${environment.apiUrl}/users/brokerage_notes/`, formData, {
      params: {
        password:fileInfo.password,
        stockBroker: fileInfo.stockBroker
      }, 
      reportProgress:true
    })
  }

  getFiles(page:number, quantity:number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/users/brokerage_notes/?page=${page}&quantity=${quantity}`);
  }

  count():Observable<any>{
    return this.http.get(`${environment.apiUrl}/users/brokerage_notes/count`);
  }

  saveInfo(file: object): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/users/brokerage_notes/`, file)
  }

  deleteFile(fileId:string): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/users/brokerage_notes/${fileId}`)
  }
}