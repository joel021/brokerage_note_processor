from extractor_service.constants import *

class OperationService:

    table_name = "operation"

    def __init__(self, mysql):
        self.mysql = mysql

    def find_oppened_by_user_id(self, user_id:str) -> list:
        cursor = self.mysql.connection.cursor()

        cursor.execute(f"""SELECT operation_id, name, type, qtd, value, date, type_op, file_id, wallet, close_month
        from {self.table_name}
            WHERE wallet in ('{BOUGHT}','{SOLD}') and user_id = uuid_to_bin('{user_id}') and deleted_at IS NULL;""")

        data = cursor.fetchall()
        openned_operations = []

        for row in data:
            openned_operations.append({
                "operation_id": row[0],
                "name": row[1],
                "type": row[2],
                "qtd": row[3],
                "value": row[4],
                "date": row[5],
                "type_op": row[6],
                "file_id": row[7],
                "wallet": row[8],
                "close_month": row[9]
                })

        cursor.close()

        return openned_operations

    def find_non_processed_by_user_id(self, user_id:str) -> list:
        cursor = self.mysql.connection.cursor()

        cursor.execute(f"""SELECT operation_id, name, type, qtd, value, date, type_op, file_id, wallet, close_month
        from {self.table_name}
            WHERE wallet is null and user_id = uuid_to_bin('{user_id}') and deleted_at IS NULL;""")

        data = cursor.fetchall()
        operations = []

        for row in data:
            operations.append({
                "operation_id": row[0],
                "name": row[1],
                "type": row[2],
                "qtd": row[3],
                "value": row[4],
                "date": row[5],
                "type_op": row[6],
                "file_id": row[7],
                "wallet": row[8],
                "close_month": row[9]
                })

        cursor.close()

        return operations

    def save_openned_operations(self, operations: list, user_id:str):

        cursor = self.mysql.connection.cursor()

        for operation in operations:

            if operation.get('operation_id', None):
                cursor.execute(f"""UPDATE {self.table_name} SET close_month = '{operation['close_month']}',
                                                    wallet = '{operation["wallet"]}', qtd = {operation['qtd']}, value = {operation['value']}
                                                    where operation_id = {operation['operation_id']}""")
            else:
                cursor.execute(
                    f"""INSERT INTO {self.table_name} (close_month, date, file_id, name, 
                                        qtd, type, type_market, type_op, value, wallet, user_id)
                                        VALUES ('{operation['close_month']}','{operation['date']}',uuid_to_bin('{operation['file_id']}'),
                                        '{operation['name']}', {operation['qtd']}, '{operation['type']}',
                                        '{operation['type_market']}','{operation['type_op']}', {operation['value']},
                                        '{operation["wallet"]}', uuid_to_bin('{user_id}'));""")

        self.mysql.connection.commit()
        cursor.close()

    def save_closed_operations(self, operations:list, user_id:str):
        cursor = self.mysql.connection.cursor()

        for operation in operations:
            
            if operation.get('operation_id', None):
                cursor.execute(f"""UPDATE {self.table_name} SET close_month = '{operation['close_month']}',
                                                wallet = 'CLOSED', qtd = {operation['qtd']}, value = {operation['value']}
                                                where operation_id = {operation['operation_id']}""")
            else:
                try:
                    cursor.execute(f"""INSERT INTO {self.table_name} (close_month, date, file_id, name,qtd, type, type_market, type_op,
                        value, wallet, user_id)
                        VALUES ('{operation['close_month']}','{operation['date']}',uuid_to_bin('{operation['file_id']}'),
                        '{operation['name']}', {operation['qtd']},'{operation['type']}','{operation['type_market']}',
                        '{operation['type_op']}', {operation['value']}, 'CLOSED', uuid_to_bin('{user_id}'));""")
                except:
                    print(operation)

        self.mysql.connection.commit()
        cursor.close()

    def delete_by_user_id(self, user_id):

        cursor = self.mysql.connection.cursor()

        cursor.execute(f"""DELETE FROM {self.table_name}
                    WHERE user_id = uuid_to_bin('{user_id}');""")

        self.mysql.connection.commit()
        cursor.close()