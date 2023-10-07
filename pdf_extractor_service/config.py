import os

DATA_PATH = "./data"

DB_HOST = os.environ['DB_HOST']
DB_PORT = os.environ['DB_PORT']
DB_USER = os.environ['DB_USER']
DB_PASSWORD = os.environ['DB_PASSWORD']
DB_DB = os.environ['DB_NAME']

LOCAL_HOST = "localhost"
PDF_EXTRACTOR_SERVICE_PORT = "1232"

LOG_FILE = "data/logging.csv"

pdf_pass = os.environ['PDF_TESTS_PASS'].split(";")
PDF_TESTS_PASS = {
    pdf_pass[0].split(":")[0]: pdf_pass[0].split(":")[1],
    pdf_pass[1].split(":")[0]: pdf_pass[1].split(":")[1],
}