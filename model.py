import uuid
from datetime import date

class User:
    user_id: uuid
    name: str
    email: str
    password: str

class PDFFile:
    file_id: uuid
    user: User
    uri: str
    password: str
    extractedAt: date
    stock_broker: str

class Operation:

    operation_id: int
    user: User
    name: str
    type: str
    qtd: int
    value: float
    date: date
    stock_broker:str
    type_op: str
    type_market:str
    file_uri: str
    wallet: str ("BOUGHT","SOLD","CLOSED")
    close_month: str
    report_id: str

