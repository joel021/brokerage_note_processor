import { Component, OnInit, Input } from "@angular/core";
import ChartModel from "src/app/_model/chart.model";

@Component({
  selector: "app-kpi",
  templateUrl: "./kpi-chart.component.html",
  styleUrls: ["./kpi-chart.component.css"],
})
export class KpiComponent implements OnInit {
  @Input() chart!: ChartModel;

  constructor() {}

  ngOnInit() {}
}
