from extractor_service.constants import SWINGTRADE, DAYTRADE, FUTURE_MARKET
from extractor_service.operations_builder.extractor import Extractor

from datetime import datetime
import re
import logging
logger = logging.getLogger(__name__)

class FutureOperationExtractor(Extractor):

    def __init__(self, errors):
        super(FutureOperationExtractor, self).__init__(errors)

    def get_operations(self, pages:list, file_id:str):

        future_op_qtd = 0
        for page in pages:
            future_op_qtd = self.__get_operations(page, file_id, future_op_qtd)

        return self.operations

    def __get_operations(self, page:str, file_id:str, initial_qtd:int) -> int:
        liquid_value = 0
        total_abs_value = 0
        op_qtd = initial_qtd
        date = None

        if "BOVESPA" in page:
            return 0

        lines = page.split("\n")

        i = 0
        for line in lines:

            line_match = re.findall(
                r"([C|V][ ][A-Z]{3}[ ]*[A-Z]*[0-9]*[ ][0-9]{2}[/][0-9]{2}[/][0-9]{4}[ ][0-9]*[ ][0-9][0-9]*[.][0-9][0-9]*[,])",
                line)

            if line_match:
                name = re.findall(r"([C|V][ ][A-Z]{3}[A-Z]*[0-9]*[ ])|$", line)
                value = self.__push_op(name, line, file_id)
                total_abs_value += abs(value)
                liquid_value += value
                op_qtd += 1

            elif "Data pregão" in line:
                if self.__continue(lines[i + 1:]):
                    return op_qtd

                date = datetime.strptime(re.findall(r"([0-9]*[/][0-9]*[/][0-9]*)|$", lines[i + 1])[0], "%d/%m/%Y")
                break

            i += 1

        if date == None:
            self.errors[file_id] = self.errors.get(file_id, [])
            self.errors[file_id].append("Erro interno: Não foi possível obter a data do pregão.")

        self.__find_net_value(op_qtd, liquid_value, date, total_abs_value, lines[i:], file_id)
        return 0

    def __push_op(self, name, line, file_id):

        name = name[0].replace(" ", "").replace("C", "").replace("V", "")
        qtd = int(re.findall(r"([ ][1-9][0-9]*[ ])|$", line)[0])

        value_str = re.findall(r"([ ][0-9][0-9]*[,][0-9]{2}[ ]*[CD])|$", line)[0]
        value = self.str_to_float(value_str.replace("C", "").replace("D", "")) * self.debit_or_credit(value_str[-1])
        type_market = FUTURE_MARKET

        if SWINGTRADE in line.replace(" ", ""):
            type_op = SWINGTRADE
        else:
            type_op = DAYTRADE

        self.operations.append({
            "name": name,
            "type": "ACTIVE",
            "qtd": qtd,
            "value": value,
            "type_op": type_op,
            "type_market": type_market,
            "file_id": file_id,
            "date": None
        })

        return value

    def __find_net_value(self, op_qtd, liquid_value, date, total_abs_value, lines, file_id):

        if op_qtd > 0:
            find_net_value = r"([0-9]*[,][0-9]*[ ]*[0-9]*[,][0-9]*[ ]*[0-9]*[,][0-9]*[ ]*[|][ ]*[0-9]*[,][0-9]*[ ]*[|][ ]*[CD][ ]*[0-9]*[,][0-9]*[ ]*[|][ ]*[CD][ ][0-9]*[,][0-9]*[ ]*[|][ ]*[CD])"

            found = []
            for line in lines:
                found = re.findall(find_net_value, line)
                if found:
                    break

            if found:
                net_value_str = re.findall(r"([ ][0-9][0-9]*[,][0-9]{2}[ ]*[|]*[ ][CD])", found[0])[-1].replace("|", "")
                net_value = self.str_to_float(
                    net_value_str.replace("C", "").replace("D", "")) * self.debit_or_credit(
                    net_value_str[-1])

                self.calc_tax(net_value, liquid_value, op_qtd, date, total_abs_value)

            else:
                logger.info(f"Can not extract float value to define net_value, file_id: {file_id}")
                self.errors[file_id] = self.errors.get(file_id, [])
                self.errors[file_id].append(
                    {"fileId": file_id, "error": "Mercado futuro: não foi possível obter o valor líquido."})

    def __continue(self, lines: list) -> bool:

        for line in lines:
            if "CONTINUA..." in line:
                return True
        return False