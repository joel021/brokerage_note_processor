import datetime

import pandas as pd
from extractor_service.constants import *
from extractor_service.operations_builder.operations_extractor import OperationsExtractor

from extractor_service.stock_option_util import Options

class OperationsBalancer():

    def __init__(self):
        self.options = Options()
        self.boughts = dict()
        self.solds = dict()
        self.closed_operations = []
        self.openned_operations = []
        self.errors = dict()

    def build(self, openned_operations:list):

        for operation in openned_operations:

            if operation['wallet'] == BOUGHT:
                self.boughts[operation['name']] = self.boughts.get(operation['name'], [])
                self.boughts[operation['name']].append(operation)

            elif operation['wallet'] == SOLD:
                self.solds[operation['name']] = self.solds.get(operation['name'], [])
                self.solds[operation['name']].append(operation)

        return self

    def from_pdf_infos(self, pdf_infos:dict) -> list:

        reports_extractor = OperationsExtractor()

        for file_info in pdf_infos['files']:
            reports_extractor.from_file_info(f"{pdf_infos['base_uri']}/{file_info['name']}", file_info['password'], file_info['file_id'])

        self.errors = reports_extractor.errors

        if reports_extractor.operations:
            return self.splited_from_operations_df(pd.DataFrame(reports_extractor.operations))
        else:
            return []

    def splited_from_operations_df(self, operations_df:pd.DataFrame) -> list:

        operations_df = operations_df.sort_values(by="date")

        operations_df.loc[:,"close_month"] = operations_df['date'].dt.strftime("%Y-%m")
        operations_df.loc[:,"date"] = operations_df['date'].dt.strftime("%Y-%m-%d")
        data_a_df = operations_df[operations_df['type'] == ACTIVE]
        data_ac_df = pd.concat([data_a_df, operations_df[operations_df['type'] == Options.CALL]])
        data_acp_df = pd.concat([data_ac_df, operations_df[operations_df['type'] == Options.PUT]])

        for i in data_acp_df.index:

            operation = operations_df.loc[i].to_dict()

            bought_stack = self.boughts.get(operation['name'], list())
            sold_stack = self.solds.get(operation['name'], list())

            if operation.get('type_op', None) == DAYTRADE: #already closed
                operation['wallet'] = CLOSED
                operation['close_month'] = operation['date'].strftime("%Y-%m")
                self.closed_operations.append(operation)
            elif operation['value'] < 0: #buy
                if sold_stack:
                    self.__balance_operations(sold_stack, bought_stack, operation)
                else:
                    bought_stack.append(operation)

            else: #sell
                if bought_stack:
                    self.__balance_operations(bought_stack, sold_stack, operation)
                else:
                    sold_stack.append(operation)

            self.boughts[operation['name']] = bought_stack
            self.solds[operation['name']] = sold_stack

        self.__dump_openned_operations(self.boughts, BOUGHT)
        self.__dump_openned_operations(self.solds, SOLD)

        return self.closed_operations

    def __balance_operations(self, reverse_op_stack:list, current_op_stack:list, operation:dict):

        while reverse_op_stack:

            op_active = reverse_op_stack.pop(0)

            if operation['qtd'] == op_active['qtd']:#abate all operations. not remain operations oppened
                self.__close_operation(operation, op_active)
                break
            elif operation['qtd'] < op_active['qtd']: #abate the current price but remain some operations openned
                #if the current operation is buy, the reverse is sell
                self.__abate_reverse(operation, op_active, reverse_op_stack)
                break
            else: #abate all oppened operations and open the remain
                self.__abate_current(operation, op_active, current_op_stack)

    def __dump_openned_operations(self, openned_stacks:dict, wallet: str):
        now = datetime.datetime.now()

        for active_stack_key in list(openned_stacks.keys()):

            for operation in openned_stacks[active_stack_key]:
                operation['type_op'] = SWINGTRADE

                if operation['type'] != ACTIVE:
                    exp_date = self.options.expiration_date(operation)

                    if now > exp_date:
                        operation['close_month'] = exp_date.strftime("%Y-%m")
                        operation['wallet'] = CLOSED
                        self.closed_operations.append(operation)
                else:
                    operation['wallet'] = wallet
                    self.openned_operations.append(operation)

    def __close_operation(self, operation, op_active):
        type_op = DAYTRADE if operation['date'] == op_active['date'] else SWINGTRADE
        operation['type_op'] = type_op
        op_active['type_op'] = type_op

        self.closed_operations.append(operation)
        op_active['close_month'] = operation['close_month']
        self.closed_operations.append(op_active)

    def __abate_reverse(self, operation, op_active, reverse_op_stack):
        remain_reverse_op = op_active.copy()
        remain_reverse_op['qtd'] -= operation['qtd']
        remain_reverse_op['value'] = remain_reverse_op['value'] * remain_reverse_op['qtd'] / op_active['qtd']

        reverse_op_stack.append(remain_reverse_op)

        op_active['value'] = op_active['value'] * operation['qtd'] / op_active['qtd']
        op_active['qtd'] = operation['qtd']

        op_active['close_month'] = operation['close_month']

        type_op = DAYTRADE if operation['date'] == op_active['date'] else SWINGTRADE
        operation['type_op'] = type_op
        op_active['type_op'] = type_op
        self.closed_operations.append(op_active)
        self.closed_operations.append(operation)

    def __abate_current(self, operation, op_active, current_op_stack):
        remain_current_op = operation.copy()
        remain_current_op['qtd'] -= op_active['qtd']
        remain_current_op['value'] = operation['value'] * remain_current_op['qtd'] / operation['qtd']

        current_op_stack.append(remain_current_op)

        operation['qtd'] = op_active['qtd']
        operation['value'] -= remain_current_op['value']

        op_active['close_month'] = operation['close_month']

        type_op = DAYTRADE if operation['date'] == op_active['date'] else SWINGTRADE
        operation['type_op'] = type_op
        op_active['type_op'] = type_op

        self.closed_operations.append(operation)
        self.closed_operations.append(op_active)