from persistence.service.extraction_error_service import ExtractionErrorService
from config import DB_HOST, DB_USER, DB_PASSWORD, DB_DB, DB_PORT
from flask_mysqldb import MySQL
from flask import Flask
import uuid
import unittest


class TestExtractionErrorService(unittest.TestCase):

    def __init__(self, methodName='runTest'):
        super().__init__(methodName)

        self.flask_app = Flask(__name__)
        self.flask_app.config['MYSQL_HOST'] = DB_HOST
        self.flask_app.config['MYSQL_USER'] = DB_USER
        self.flask_app.config['MYSQL_PASSWORD'] = DB_PASSWORD
        self.flask_app.config['MYSQL_DB'] = DB_DB
        self.flask_app.config['MYSQL_PORT'] = DB_PORT
        self.flask_app.config['DEBUG'] = False

        self.mysql_context = MySQL(self.flask_app)

        self.extraction_error_service = ExtractionErrorService(self.mysql_context)

    def tearDown(self):

        with self.flask_app.app_context():
            cursor = self.mysql_context.connection.cursor()

            for key in self.errorsTest.keys():
                cursor.execute(f"""DELETE FROM {self.extraction_error_service.table_name} 
                                    WHERE file_id = uuid_to_bin('{key}');""")

            self.mysql_context.connection.commit()
            cursor.close()

    def test_save_from_dict(self):
        self.errorsTest = {
            uuid.uuid1(): ["Error", "Error", "Error", "Error"]
        }
        with self.flask_app.app_context():
            self.extraction_error_service.save_from_dict(self.errorsTest)


if __name__ == '__main__':
    unittest.main()
