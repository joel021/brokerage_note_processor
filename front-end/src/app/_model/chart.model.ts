import { DataGrouped } from "./data-grouped.model";
import StatusChartData from "./status-chart-data.model";

export default class ChartModel {
  private _data: number[] | number | DataGrouped[];
  status: StatusChartData = new StatusChartData();
  getDataFunction: Function;
  label: string;
  columns?: string[];
  ctx: string;

  constructor(label: string, columns?: string[], ctx?: string) {
    this.label = label;
    this.columns = columns;
    this.ctx = ctx;
    this._data = null
  }

  public set data(new_data: number[] | number | DataGrouped[]) {
    this.status.setSucess();
    this._data = new_data;

    if (Array.isArray(new_data) && new_data.length == 0) {
      this.status.empty = true;
    } else if (!new_data) {
      this.status.empty = true;
    }
  }

  public get data() {
    return this._data;
  }
}
