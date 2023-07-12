from extractor_service.operations_builder.operations_balancer import OperationsBalancer
from extractor_service.constants import ACTIVE, SWINGTRADE, CASH_MARKET, SOLD, BOUGHT

import unittest
import datetime
import uuid
import pandas as pd


class TestOperationsBalancer(unittest.TestCase):


    def test_close_operations(self):

        openned_operations = []
        openned_operations.append({
            "name": "ACTIVE1",
            "type": ACTIVE,
            "qtd": 200,
            "value": 400.0,
            "date": datetime.datetime.strptime("2023-01-01", "%Y-%m-%d"),
            "type_op": SWINGTRADE,
            "type_market": CASH_MARKET,
            "file_id": uuid.uuid1(),
            "wallet": SOLD,
            "close_month": None
        })

        openned_operations.append({
            "name": "ACTIVE2",
            "type": ACTIVE,
            "qtd": 200,
            "value": 400.0,
            "date": datetime.datetime.strptime("2023-01-01", "%Y-%m-%d"),
            "type_op": SWINGTRADE,
            "type_market": CASH_MARKET,
            "file_id": uuid.uuid1(),
            "wallet": SOLD,
            "close_month": None
        })

        operations = []
        operations.append({
            "name": "ACTIVE1",
            "type": ACTIVE,
            "qtd": 200,
            "value": 400.0,
            "date": datetime.datetime.strptime("2023-01-02", "%Y-%m-%d"),
            "type_op": SWINGTRADE,
            "type_market": CASH_MARKET,
            "file_id": uuid.uuid1().hex,
            "wallet": SOLD,
            "close_month": None
        })

        operations.append({
            "name": "ACTIVE2",
            "type": ACTIVE,
            "qtd": 100,
            "value": -100.0,
            "date": datetime.datetime.strptime("2023-01-02", "%Y-%m-%d"),
            "type_op": SWINGTRADE,
            "type_market": CASH_MARKET,
            "file_id": uuid.uuid1().hex,
            "wallet": BOUGHT,
            "close_month": None
        })

        operations_balancer = OperationsBalancer().build(openned_operations)
        closed_operations = operations_balancer.splited_from_operations_df(pd.DataFrame(operations))

        assert len(closed_operations) == 2
        assert len(operations_balancer.openned_operations) == 3
        assert pd.DataFrame(closed_operations)['value'].sum() == 100



if __name__ == '__main__':
    unittest.main()