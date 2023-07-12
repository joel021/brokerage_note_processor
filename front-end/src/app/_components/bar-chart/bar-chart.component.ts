import { Component, Input, KeyValueDiffers, DoCheck } from "@angular/core";
import * as Chart from "chart.js";
import ChartModel from "../../_model/chart.model"

@Component({
  selector: "app-bar-chart",
  templateUrl: "./bar-chart.component.html",
  styleUrls: ["./bar-chart.component.css"],
})
export class BarChartComponent implements DoCheck {
  @Input() chart: ChartModel;
  @Input() type: String = "horizontal"
  differ: any;

  constructor(private differs: KeyValueDiffers) {
    this.differ = differs.find({}).create();
  }

  ngDoCheck() {
    var changes = this.differ.diff(this.chart);

    if (changes) {
      if (this.chart.data) {
        setTimeout(() => {
          this.generateValuesGroupedChart(this.chart.data);
        }, 200);
      }
    }
  }

  generateValuesGroupedChart(valuesGrouped) {
    return new Chart(this.chart.ctx || this.chart.label, {
      type: "horizontalBar",
      data: {
        labels: valuesGrouped.map((item) => item[0]),
        datasets: [
          {
            label: this.chart.columns[0],
            data: valuesGrouped.map((item) => item[1]),
            borderWidth: 1
          },
        ],
      },
      options: {
        scales: {
            xAxes: [{
                ticks: {
                    beginAtZero: true
                }
            }]
        }
      }
    });
  };
}
