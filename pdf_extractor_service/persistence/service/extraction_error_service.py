
class ExtractionErrorService:

    table_name = "extraction_error"

    def __init__(self, mysql):
        self.mysql = mysql

    def save_from_dict(self, errors:dict):
        cursor = self.mysql.connection.cursor()

        for key in errors.keys():

            for error in errors[key]:
                cursor.execute(f"""INSERT INTO {self.table_name} (error, file_id)
                    VALUES ('{error}',uuid_to_bin('{key}'));""")

        self.mysql.connection.commit()
        cursor.close()

    def find_by_file_id(self, file_id):

        cursor = self.mysql.connection.cursor()

        cursor.execute(f"""SELECT extraction_error_id, error
                from {self.table_name}
                    WHERE file_id = uuid_to_bin('{file_id}');""")

        data = cursor.fetchall()
        errors = []

        for row in data:
            errors.append({
                "extraction_error_id": row[0],
                "error": row[1],
                "file_id": file_id
            })

        cursor.close()
        return errors

    def delete_by_file_id(self, file_id):
        cursor = self.mysql.connection.cursor()

        cursor.execute(f"""DELETE FROM {self.table_name}
                            WHERE file_id = uuid_to_bin('{file_id}');""")

        self.mysql.connection.commit()
        cursor.close()