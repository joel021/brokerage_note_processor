from extractor_service.stock_option_util import Options

class Extractor():

    def __init__(self, errors=None):

        if errors is None:
            errors = {}

        self.operations = []
        self.errors = errors
        self.options = Options()

    def str_to_float(self, value):
        return float(value.replace(" ", "")
                     .replace(".", "")
                     .replace(",", "."))

    def debit_or_credit(self, c: str) -> int:
        if c.upper() == "D":
            return -1
        else:
            return 1

    def calc_tax(self, net_value:float, liquid_value:float, op_qtd:int, date, total_abs_value:float):
        tax = (net_value - liquid_value)
        len_op = len(self.operations)

        for i in range(len_op - op_qtd, len_op):
            eq_tax = tax * abs(self.operations[i]["value"]) / total_abs_value
            self.operations[i]["value"] += eq_tax
            self.operations[i]["date"] = date
