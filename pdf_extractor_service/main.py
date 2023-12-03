from config import LOG_FILE, LOCAL_HOST, PDF_EXTRACTOR_SERVICE_PORT, DB_HOST, DB_USER, DB_PASSWORD, DB_DB, DB_PORT
from flask_mysqldb import MySQL
from flask import Flask
from flask import request, Response
from waitress import serve

from extractor_service.operations_extractor.operations_extractor import OperationsExtractor
from persistence.service.file_service import FileService
from persistence.service.operation_service import OperationService
from persistence.service.extraction_error_service import ExtractionErrorService

import pdf_extractor_app

import datetime
import logging
logger = logging.getLogger(__name__)

logging.basicConfig(filename=LOG_FILE,
                    filemode='a',
                    format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
                    datefmt='%H:%M:%S',
                    level=logging.DEBUG)


app = Flask(__name__)
app.config['MYSQL_HOST'] = DB_HOST
app.config['MYSQL_USER'] = DB_USER
app.config['MYSQL_PASSWORD'] = DB_PASSWORD
app.config['MYSQL_DB'] = DB_DB
app.config['MYSQL_PORT'] = DB_PORT
app.config['DEBUG'] = False

mysql_context = MySQL(app)

file_service = FileService(mysql_context)
operation_service = OperationService(mysql_context)
error_service = ExtractionErrorService(mysql_context)

@app.route("/brokerage_note", methods=['POST'])
def process_pdf():

    user_info = request.get_json()
    
    print("Received:")
    print(user_info)

    user_pdfs_info = {
        "user_id":  user_info['userId'],
        "base_uri": user_info['baseUri'],
        "files": file_service.find_by_user_id(user_info['userId'])
    }
    operation_extractor = OperationsExtractor()
    operation_service.save_operations(operations, user_pdfs_info['userId'])

    user_pdfs_info['extracted_at'] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    file_service.set_extracted_at_from_infos(user_pdfs_info)
    error_service.save_from_dict(operations_extractor.errors)

    return Response("", status=204, mimetype='application/json')


@app.route("/update_operations", methods=["POST"])
def update_operations():
    
    user_information = request.get_json()

    operations = operation_service.find_oppened_by_user_id(user_information['userId'])

    operation_service.save_closed_operations(operations_balancer.closed_operations, user_information['userId'])
    operation_service.save_openned_operations(operations_balancer.openned_operations, user_information['userId'])

    return Response(status=204, headers={})


if __name__ == "__main__":
    logger.info("Extractor Listener Service started.")
    print(f"PDF Extractor Service at: {LOCAL_HOST}:{PDF_EXTRACTOR_SERVICE_PORT}")
    serve(pdf_extractor_app.app, listen=f"{LOCAL_HOST}:{PDF_EXTRACTOR_SERVICE_PORT}")
