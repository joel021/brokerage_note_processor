from config import *
from manager_service.db.file_service import FileService
from manager_service.db.operation_service import OperationService
from extractor_service.operations_builder.operations_balancer import OperationsBalancer

from flask import Flask, request, Response
from flask_mysqldb import MySQL

from multiprocessing import Queue
from threading import Thread
from waitress import serve

from pandas import DataFrame

import logging
logger = logging.getLogger(__name__)

class OperationsController():

    class EndpointAction(object):

        def __init__(self, action):
            self.action = action

        def __call__(self, *args):
            if request.method == "GET":
                Thread(target=self.action, args=(request.args,)).start()
            else:
                Thread(target=self.action, args=(request.get_json(),)).start()

            return Response(status=204, headers={})

    flask_app = None

    def __init__(self, port=None):

        if port is None:
            self.port = PDF_EXTRACTOR_SERVICE_PORT
        else:
            self.port = port

        self.flask_app = Flask(f"extractor_listener_{PDF_EXTRACTOR_SERVICE_PORT}")

        self.flask_app.config['MYSQL_HOST'] = DB_HOST
        self.flask_app.config['MYSQL_USER'] = DB_USER
        self.flask_app.config['MYSQL_PASSWORD'] = DB_PASSWORD
        self.flask_app.config['MYSQL_DB'] = DB_DB
        self.flask_app.config['MYSQL_PORT'] = DB_PORT
        self.flask_app.config['DEBUG'] = False

        mysql_context = MySQL(self.flask_app)

        self.file_service = FileService(mysql_context)
        self.operation_service = OperationService(mysql_context)

        self.flask_app.add_url_rule("/brokerage_note", "process", self.EndpointAction(self.extract_operations), methods=["POST"])
        self.flask_app.add_url_rule("/update_operations", "update", self.EndpointAction(self.update_operations), methods=["POST"])
        self.flask_app.add_url_rule("/finish", "finish", self.EndpointAction(self.finish), methods=["POST"])
    def extract_operations(self, resp):
        with self.flask_app.app_context():
            files = self.file_service.find_by_user_id(resp['userId'])
        self.extractor_queue.put({"user_id": resp['userId'], "base_uri":resp['baseUri'], "files": files})

    def update_operations(self, object):

        operations_balancer = OperationsBalancer()

        with self.flask_app.app_context():
            operations_balancer.build(self.operation_service.find_oppened_by_user_id(object['userId']))
            operations = self.operation_service.find_non_processed_by_user_id(object['userId'])

        if operations:
            operations_balancer.splited_from_operations_df(DataFrame(operations))

        with self.flask_app.app_context():
            self.operation_service.save_closed_operations(operations_balancer.closed_operations, object['userId'])
            self.operation_service.save_openned_operations(operations_balancer.openned_operations, object['userId'])
    def finish(self, args):
        self.extractor_queue.put({"finish": True})

    def run(self, extractor_queue: Queue):
        logger.info("Extractor Listener Service started.")
        self.extractor_queue = extractor_queue
        print(f"PDF Extractor Service at: {LOCAL_HOST}:{self.port}")
        serve(self.flask_app, listen=f"{LOCAL_HOST}:{self.port}")