import datetime

from manager_service.operations_controller import OperationsController
from extractor_service.operations_builder.operations_balancer import OperationsBalancer
from config import DB_HOST, DB_USER, DB_PASSWORD, DB_DB, DB_PORT, LOG_FILE
from manager_service.db.file_service import FileService
from manager_service.db.operation_service import OperationService
from manager_service.db.extraction_error_service import ExtractionErrorService

from multiprocessing import Process, Queue
from flask import Flask
from flask_mysqldb import MySQL
from threading import Thread

class PDFExtractorManager():

    def __init__(self ):
        self.flask_app = Flask(__name__)
        self.flask_app.config['MYSQL_HOST'] = DB_HOST
        self.flask_app.config['MYSQL_USER'] = DB_USER
        self.flask_app.config['MYSQL_PASSWORD'] = DB_PASSWORD
        self.flask_app.config['MYSQL_DB'] = DB_DB
        self.flask_app.config['MYSQL_PORT'] = DB_PORT
        self.flask_app.config['DEBUG'] = False

        mysql_context = MySQL(self.flask_app)

        self.file_service = FileService(mysql_context)
        self.operation_service = OperationService(mysql_context)
        self.error_service = ExtractionErrorService(mysql_context)
    def extract(self, pdfs_info):

        operations_balancer = OperationsBalancer()
        with self.flask_app.app_context():
            operations_balancer.build(self.operation_service.find_oppened_by_user_id(pdfs_info['user_id']))

        closed_operations = operations_balancer.from_pdf_infos(pdfs_info)

        with self.flask_app.app_context():
            self.operation_service.save_closed_operations(closed_operations, pdfs_info['user_id'])
            self.operation_service.save_openned_operations(operations_balancer.openned_operations, pdfs_info['user_id'])

        pdfs_info['extracted_at'] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        with self.flask_app.app_context():
            self.file_service.set_extracted_at_from_infos(pdfs_info)
            self.error_service.save_from_dict(operations_balancer.errors)
class PDFsStackMain():

    def __init__(self):
        self.files_queue = Queue()
        self.free = Queue()

    def start_listener(self, listener_topic_queue, port=None):
        import logging
        logging.basicConfig(filename=LOG_FILE,
                            filemode='a',
                            format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
                            datefmt='%H:%M:%S',
                            level=logging.DEBUG)

        publisher_subscriber = OperationsController(port)
        publisher_subscriber.run(listener_topic_queue)

    def main(self, port=None):
        listener_topic_queue = Queue()

        p_listener = Process(target=self.start_listener, args=(listener_topic_queue,port,))
        p_listener.start()

        extractorManager = PDFExtractorManager()

        while True:

            to_process_info = listener_topic_queue.get(block=True)

            if to_process_info.get("finish", False):
                break
            Thread(target=extractorManager.extract, args=(to_process_info, )).start()

        p_listener.terminate()
        p_listener.join()
