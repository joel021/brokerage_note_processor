export default class StatusChartData {
    empty: boolean = true;
    fetching: boolean = false;
    erro: boolean = false;
    sucess: boolean = false;
  
    get isEmpty() {
      return this.empty;
    }
  
    get isFetching() {
      return this.fetching;
    }
  
    get hasErrors() {
      return this.erro;
    }
  
    get isSuccessful() {
      return this.sucess;
    }
  
    setError() {
      this.empty = false;
      this.fetching = false;
      this.erro = true;
      this.sucess = false;
    }
  
    setFetching() {
      this.empty = false;
      this.fetching = true;
      this.erro = false;
      this.sucess = false;
    }
  
    setSucess() {
      this.empty = false;
      this.fetching = false;
      this.erro = false;
      this.sucess = true;
    }
  }
  