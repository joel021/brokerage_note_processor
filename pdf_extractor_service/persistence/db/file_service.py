class FileService:
    table_name = "file"

    def __init__(self, mysql):
        self.mysql = mysql

    def save_from_list(self, files: list):

        cursor = self.mysql.connection.cursor()

        for file in files:
            cursor.execute(f"""INSERT INTO {self.table_name} (file_id, user_id, name, password)
                                        VALUES (uuid_to_bin('{file["file_id"]}'), uuid_to_bin('{file["user_id"]}'), 
                                        '{file['name']}', '{file["password"]}');""")

        self.mysql.connection.commit()
        cursor.close()

    def delete_by_user_id(self, user_id):
        cursor = self.mysql.connection.cursor()
        cursor.execute(f"""DELETE FROM {self.table_name} WHERE user_id = UUID_TO_BIN('{user_id}');""")
        self.mysql.connection.commit()
        cursor.close()

    def find_by_user_id(self, user_id:str) -> list:
        cursor = self.mysql.connection.cursor()

        cursor.execute(f"""SELECT BIN_TO_UUID(file_id), name, password
                        from {self.table_name} 
                        WHERE extracted_at is null and deleted_at is null and user_id = UUID_TO_BIN('{user_id}');""")

        data = cursor.fetchall()
        result = []

        for row in data:
            result.append({"file_id": row[0],
                          "name": row[1],
                          "password": row[2]
                          })

        cursor.close()

        return result

    def set_extracted_at_from_infos(self, pdf_infos:dict):

        if pdf_infos['files']:

            cursor = self.mysql.connection.cursor()

            for pdf_file in pdf_infos['files']:
                cursor.execute(f"""UPDATE {self.table_name} SET extracted_at = '{pdf_infos['extracted_at']}' 
                    where file_id = uuid_to_bin('{pdf_file['file_id']}');""")

            self.mysql.connection.commit()
            cursor.close()