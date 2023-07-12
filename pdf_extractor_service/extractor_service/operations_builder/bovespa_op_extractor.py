from extractor_service.constants import OPTION_MARKET, ACTIVES, CASH_MARKET
from extractor_service.operations_builder.extractor import Extractor

from datetime import datetime
import re
import logging
from numpy import min as np_min

logger = logging.getLogger(__name__)

class BovespaOperationExtractor(Extractor):

    def __init__(self, errors):
        super(BovespaOperationExtractor, self).__init__(errors)

    def get_operations(self, pages: list, file_id):
        for page in pages:
            self.__get_operations(page.split("BOVESPA")[1:], file_id)

        return self.operations

    def __get_operations(self, operations_text: list, file_id: str = None):
        op_qtd = len(operations_text)

        if op_qtd == 0:
            return

        post_liquid = operations_text[-1][operations_text[-1].find("Líquido para ") + 13:]
        date = datetime.strptime(re.findall(r"([0-9]*[/][0-9]*[/][0-9]*)|$", post_liquid)[0], "%d/%m/%Y")

        liquid_value = 0
        total_abs_value = 0

        for operation_line in operations_text:
            value = self.__push_operation(operation_line, date, file_id)
            liquid_value += value
            total_abs_value += abs(value)

        net_value = self.__get_net_value(liquid_value, post_liquid, file_id)

        self.calc_tax(net_value, liquid_value, op_qtd, date, total_abs_value)

    def __get_vista_op(self, operation_line:str):
        name = \
        re.findall(r"([ ][A-Z]{1}[A-Z]*[0-9]*[ ][A-Z]{2})|$", operation_line[operation_line.index("VISTA") + 5:])[0][1:]
        name_ = ACTIVES.get(" ".join(name.split()), name)
        stop_len = len(name_)

        for key in ACTIVES.keys():
            if name_ in ACTIVES[key][0:stop_len + 1]:
                name = key
                break

        actives_code = [k for k, v in ACTIVES.items() if v in name_]
        if actives_code:
            name = actives_code[0]

        type_ = "ACTIVE"
        type_market = CASH_MARKET

        return name, type_, type_market

    def __get_exec_option_op(self, operation_line:str):
        option_name = re.findall(r"([0-9]{2}[/][0-9]{2}[ ][A-Z]*[0-9]*)|$", operation_line)[0][5:].replace(" ","")
        return self.options.active_name(option_name), "ACTIVE", OPTION_MARKET

    def __get_value(self, operation_line):
        value_str = re.findall(r"([0-9]*[.]?[0-9]*[,][0-9]{2}[ ][CD])|$",
                               operation_line)[0].replace(" ", "")
        return self.str_to_float(value_str[0:-1]) * self.debit_or_credit(value_str[-1])

    def __get_net_value(self, liquid_value:float, post_liquid:str, file_id:str) -> float:
        digits = len(str(int(liquid_value))) - 1
        c_or_d = "C" if liquid_value > 0 else "D"
        values = re.findall(r"(([0-9]*[.]?[0-9][0-9]*){" + str(digits) + "}[,][0-9]{2}[ ]?[" + c_or_d + "])",
                            post_liquid)
        values_float = []
        for value in values:
            try:
                values_float.append(self.str_to_float(value[0][:-1]) * self.debit_or_credit(value[0][-1]))
            except:
                pass

        if not values_float:
            logger.info(f"Can't extract float value to define net_value, file_id: {file_id}")
            self.errors[file_id] = self.errors.get(file_id, [])
            self.errors[file_id].append({"fileId": file_id, "error": "Uma operação não esperada."})
            return liquid_value
        else:
            return np_min(values_float)

    def __push_operation(self, operation_line:str, date:datetime, file_id:str) -> float:
        if "OPCAO DE " in operation_line or " OPCAODE " in operation_line or "OPÇÃO DE" in operation_line:
            name = re.findall(r"([0-9]{2}[/][0-9]{2}[ ][A-Z]*[0-9]*)|$", operation_line)[0].split(" ")[1].replace(" ",
                                                                                                                  "")
            type_ = "CALL" if "COMPRA" in operation_line else "PUT"
            type_market = OPTION_MARKET
        elif "VISTA" in operation_line:
            name, type_, type_market = self.__get_vista_op(operation_line)
        elif "EX OPC DE " in operation_line or "EXERC OPC" in operation_line or "EXERCOPC" in operation_line:
            name, type_, type_market = self.__get_exec_option_op(operation_line)
        else:
            self.errors[file_id] = self.errors.get(file_id, [])
            self.errors[file_id].append({"fileId": file_id, "error": "Uma operação não esperada."})
            logger.critical(f"Unexpected operation with fileId: {file_id}")
            return 0

        qtd = int(re.findall(r"([ ][0-9]*[ ])", operation_line)[0].replace(" ", ""))
        value = self.__get_value(operation_line)

        self.operations.append({
            "name": name,
            "type": type_,
            "qtd": qtd,
            "value": value,
            "date": date,
            "type_op": None,
            "type_market": type_market,
            "file_id": file_id
        })

        return value