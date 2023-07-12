import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { FetchGenericService } from './fetch.generic.service';

@Injectable({ providedIn: 'root' })
export class ExtractionErrorService extends FetchGenericService {

  resourcePath = 'users/brokerage_notes/errors';

  constructor(public http: HttpClient) {
    super(http);
  }

  create(extractionError: any): Observable<any> {
    return super.save(`${this.resourcePath}/`, extractionError);
  }

  getByFileId(fileId: any): Observable<any> {
    return super.readById(`${this.resourcePath}/by_file_id`, fileId);
  }

  getCountByFileId(fileId: any): Observable<any> {
    return super.readById(`${this.resourcePath}/count_by_file_id`, fileId);
  }

  put(address: any): Observable<any> {
    return super.put(this.resourcePath, address);
  }

  deleteById(addressId: any): Observable<any> {
    return super.remove(this.resourcePath, addressId);
  }
}
