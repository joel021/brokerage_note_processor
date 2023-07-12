from config import PDF_TESTS_PASS
from extractor_service.operations_builder.operations_extractor import OperationsExtractor

import uuid
import unittest
import glob
import os

class TestOperationsExtractor(unittest.TestCase):

    def test_from_file_info(self):

        pdfs_to_test_folder = "data/pdfs_to_test/brokerage_with_pass"

        operations_extractor = OperationsExtractor()

        for file_uri in glob.glob(f"{pdfs_to_test_folder}/*.pdf"):

            password = PDF_TESTS_PASS.get(os.path.split(file_uri)[1].split("_")[0], None)
            if not password:
                continue

            operations_extractor.from_file_info(pdf_uri=file_uri,
                                                password=password,
                                                file_id=uuid.uuid1().hex)

            for error_key in operations_extractor.errors.keys():
                self.assertTrue(len(operations_extractor.errors[error_key]) == 0, operations_extractor.errors[error_key])

            assert operations_extractor.operations

    def test_extract_when_pass_wrong(self):

        pdfs_to_test_folder = "data/pdfs_to_test/brokerage_with_pass"
        operations_extractor = OperationsExtractor()

        for file_uri in glob.glob(f"{pdfs_to_test_folder}/*.pdf"):
            operations_extractor.from_file_info(pdf_uri=file_uri,
                                                password="wrong_pass",
                                                file_id=uuid.uuid1().hex)
            assert operations_extractor.errors

    def test_with_random_pdf(self):

        extractor = OperationsExtractor()

        for random_pdf_uri in glob.glob("data/pdfs_to_test/random_pdfs/*.pdf"):
            operations = extractor.from_file_info(random_pdf_uri, None, uuid.uuid1().hex)
            assert not operations

if __name__ == '__main__':
    unittest.main()