import unittest

from config import PROJECT_LOCAL
from extractor_service.pdf_extraction.pdf_extractor import retrieve_pages


class PdfExtractorTests(unittest.TestCase):

    def __init__(self, methodName='runTest'):
        super().__init__(methodName)

    def test_retrieve_pages(self):
        errors, pages = retrieve_pages(f"{PROJECT_LOCAL}/data/pdfs/cm_report.pdf", "07577")
        self.assertTrue(len(pages) > 1)

    def test_retrieve_pages_and_print(self):
        errors, pages = retrieve_pages(f"{PROJECT_LOCAL}/data/pdfs/cm_report.pdf", "07577")

        for page in pages:
            print(page)


if __name__ == '__main__':
    unittest.main()
