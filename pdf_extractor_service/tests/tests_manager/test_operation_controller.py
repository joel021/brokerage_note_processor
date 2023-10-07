import time

from config import DB_HOST, DB_USER, DB_PASSWORD, DB_DB, DB_PORT, LOCAL_HOST, PDF_TESTS_PASS
from persistence.db.file_service import FileService
from persistence.db.operation_service import OperationService
from persistence.db.extraction_error_service import ExtractionErrorService

from threading import Thread
from flask_mysqldb import MySQL
from flask import Flask
import uuid
import unittest
import os
from glob import glob
import requests

class TestOperationController(unittest.TestCase):

    def __init__(self, methodName='runTest'):
        super().__init__(methodName)

        self.flask_app = Flask(__name__)
        self.flask_app.config['MYSQL_HOST'] = DB_HOST
        self.flask_app.config['MYSQL_USER'] = DB_USER
        self.flask_app.config['MYSQL_PASSWORD'] = DB_PASSWORD
        self.flask_app.config['MYSQL_DB'] = DB_DB
        self.flask_app.config['MYSQL_PORT'] = DB_PORT
        self.flask_app.config['DEBUG'] = False

        mysql_context = MySQL(self.flask_app)
        self.user_id = uuid.uuid1().hex
        self.file_service = FileService(mysql_context)
        self.operations_service = OperationService(mysql_context)
        self.extraction_error_service = ExtractionErrorService(mysql_context)

        self.base_uri = "data/pdfs_to_test/brokerage_with_pass"

    def __on_start(self, port) -> None:

        Thread(target=start, args=(port, )).start()

        files = []
        for pdf_uri in glob(self.base_uri + "/*.pdf"):

            pdf_name = os.path.split(pdf_uri)[1]
            password = PDF_TESTS_PASS.get(pdf_name.split("_")[0], None)
            if not password:
                continue

            files.append({
                "file_id": uuid.uuid1().hex,
                "user_id": self.user_id,
                "name": pdf_name,
                "password": password
            })

        with self.flask_app.app_context():
            self.file_service.save_from_list(files)
            self.files = self.file_service.find_by_user_id(self.user_id)

    def test_brokerage_note(self):
        port = 1235
        self.__on_start(port)

        time.sleep(5)
        resp = requests.post(f"http://{LOCAL_HOST}:{port}/brokerage_note",
            json = {
                "baseUri": self.base_uri,
                "userId": self.user_id
            }
        )
        self.assertTrue(resp.status_code == 204)

        time.sleep(5)

        for file in self.files:
            with self.flask_app.app_context():
                errors = self.extraction_error_service.find_by_file_id(file['file_id'])

            self.assertTrue(not errors, errors)

        self.__on_finish(port)

    def test_too_many_requests(self):

        port = 1234
        self.__on_start(port)

        for i in range(0,2):
            resp = requests.post(f"http://{LOCAL_HOST}:{port}/brokerage_note",
                json = {
                    "baseUri": self.base_uri,
                    "userId": self.user_id
                }
            )
            self.assertTrue(resp.status_code == 204)

        self.__on_finish(port)

    def __on_finish(self, port):

        try:
            requests.post(f"http://{LOCAL_HOST}:{port}/finish", json={})
        except:
            pass

        with self.flask_app.app_context():
            self.file_service.delete_by_user_id(self.user_id)
            self.operations_service.delete_by_user_id(self.user_id)

            for file in self.files:
                self.extraction_error_service.delete_by_file_id(file['file_id'])
def start(port=None):
    from persistence.main_file_stack_hundler import PDFsStackMain
    clicker_service = PDFsStackMain()
    clicker_service.main(port)

if __name__ == '__main__':
    unittest.main()
