from extractor_service.operations_extractor.bovespa_op_extractor import BovespaOperationUtils
from extractor_service.operations_extractor.future_op_extractor import FutureOperationUtils
from extractor_service.pdf_extraction.pdf_extractor import retrieve_pages
import logging
logger = logging.getLogger(__name__)

class OperationsExtractor:

    def __init__(self):
        self.errors = {}
        self.operations = []

    def __from_file_info(self, pdf_uri: str, password: str, file_id: str) -> list:

        errors, pages = retrieve_pages(pdf_uri, password)
        self.errors[file_id] = errors

        future_operation_extractor = FutureOperationUtils(self.errors)
        self.operations = future_operation_extractor.get_operations(pages, file_id)

        bovespa_operation_extractor = BovespaOperationUtils(self.errors)
        self.operations += bovespa_operation_extractor.get_operations(pages, file_id)

        return self.operations

    def from_pdf_infos(self, pdfs_infos:dict) -> (list, dict):

        for pdf_info in pdfs_infos['files']:
            self.__from_file_info(f"{pdfs_infos['base_uri']}/{pdf_info['name']}", pdf_info['password'], pdf_info['file_id'])

        return self.operations, self.errors
