import unittest

from config import PROJECT_LOCAL
from extractor_service.operations_extractor.operations_extractor import OperationsExtractor


class OperationsExtractorTests(unittest.TestCase):

    def __init__(self, methodName='runTest'):
        super().__init__(methodName)


    def setUp(self):

        self.pdfs_infos = {
            "files" : [{
                "name": "cm_report.pdf",
                "password": "07577",
                "file_id": "9309dhdj900j8e9hd09j.d3f"
            }],
            "base_uri": f"{PROJECT_LOCAL}/data/pdfs"
        }

    def test_from_pdf_infos(self):

        operations_extractor = OperationsExtractor()
        operarations, errors = operations_extractor.from_pdf_infos(self.pdfs_infos)

        self.assertTrue(len(operarations) > 0)

    def test_from_pdf_infos_no_errors(self):

        operations_extractor = OperationsExtractor()
        operarations, errors = operations_extractor.from_pdf_infos(self.pdfs_infos)

        self.assertTrue(len(errors) == 0)

    def test_from_pdf_infos_print(self):

        operations_extractor = OperationsExtractor()
        operarations, errors = operations_extractor.from_pdf_infos(self.pdfs_infos)

        for operation in operarations:
            print(operation)

    def test_from_pdf_infos_print_errors(self):

        operations_extractor = OperationsExtractor()
        operarations, errors = operations_extractor.from_pdf_infos(self.pdfs_infos)

        for key in errors.keys():

            for error in errors[key]:
                print(error)

if __name__ == '__main__':
    unittest.main()
