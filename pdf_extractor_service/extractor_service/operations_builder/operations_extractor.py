from extractor_service.operations_builder.bovespa_op_extractor import BovespaOperationExtractor
from extractor_service.operations_builder.future_op_extractor import FutureOperationExtractor
from extractor_service.operations_builder.pdf_utils import retrieve_pages
import logging
logger = logging.getLogger(__name__)

class OperationsExtractor:

    def __init__(self):
        self.errors = {}
        self.operations = []

    def from_file_info(self, pdf_uri: str, password: str, file_id: str) -> list:
        errors, pages = retrieve_pages(pdf_uri, password)
        self.errors[file_id] = errors

        future_operation_extractor = FutureOperationExtractor(self.errors)
        self.operations = future_operation_extractor.get_operations(pages, file_id)

        bovespa_operation_extractor = BovespaOperationExtractor(self.errors)
        self.operations += bovespa_operation_extractor.get_operations(pages, file_id)

        return self.operations
