import os.path
import unittest
import glob

from extractor_service.operations_builder.pdf_utils import retrieve_pages
from config import PDF_TESTS_PASS

class TestPdfOpenner(unittest.TestCase):

    def test_retrieve_pages(self):

        for pdf_uri in glob.glob("./data/pdfs_to_test/brokerage_with_pass/*.pdf"):

            password = PDF_TESTS_PASS.get(os.path.split(pdf_uri)[1].split("_")[0], None)
            if not password:
                continue

            errors, pages = retrieve_pages(pdf_uri, password)
            assert not errors
            
if __name__ == "__main__":

   unittest.main()