import { Activity } from "src/app/_model/activity.interface";
import ChartModel from "src/app/_model/chart.model";
import { Component, DoCheck,Input,KeyValueDiffers} from "@angular/core";


@Component({
    selector: "app-table-chart",
    templateUrl: "./table-chart.component.html",
    styleUrls: ["./table-chart.component.css"],
})
export class TableChart implements DoCheck {
    @Input() chart: ChartModel;
    @Input() context: Activity

    differ: any;

    constructor(private differs: KeyValueDiffers) {
        this.differ = differs.find({}).create();
    }

    ngDoCheck() {
        var changes = this.differ.diff(this.chart);
    }

    clickRow(i){
        this.context.callBackComponent({
            activeFilter: this.chart.data[i][0]
        })
    }
}
