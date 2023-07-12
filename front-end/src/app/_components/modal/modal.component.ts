import { Component, OnInit, Input} from '@angular/core'
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';


import { Activity } from '../../_model/activity.interface';

@Component({
    selector: 'modal-dialog',
    templateUrl: './modal.component.html'
})
export class ModalDialog implements OnInit {

    @Input() disabled: boolean
    @Input() buttonClass: string
    @Input() triggerText: string
    @Input() title: string
    @Input() body: string
    @Input() acceptValue: String
    @Input() notAcceptValue: String

    @Input() context: Activity
    @Input() method: string
    @Input() callbackArg: String

    closeResult = ""
    constructor(
        private modalService: NgbModal
    ) {
    }

    ngOnInit() {

    }

    open(content:any) {
        this.modalService.open(content, {ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
          var object = {}
          object["method"] = this.method
          object["arg"] = this.callbackArg
          this.context.callBackComponent(object)
        }, (reason) => {
          
        });
      }
}
