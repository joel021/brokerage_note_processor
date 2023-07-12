import re

from extractor_service.operations_builder.pdf_utils import retrieve_pages
from glob import glob

class DarfHandler:

    def read_darfs_from_folder(self, folder_uri:str) -> list:
        darfs = []
        for pdf_uri in glob(f"{folder_uri}/*.pdf"):
            darfs.append(self.read_darf_from_pdf(pdf_uri))

        return darfs

    def read_darf_from_pdf(self, pdf_uri:str) -> dict:

        darf = dict()
        pages = retrieve_pages(pdf_uri)

        if pages:
            page = pages[0]
            page_p1 = page[page.find("DE APURACAO")+11:]
            darf['calculation_date'] = re.findall(r"([0-9]{2}[/][0-9]{2}[/][0-9]{4})", page_p1)[0]

            darf["total_value"] = float(re.findall(r"([0-9]*[0-9][,][0-9]{2})", page_p1[page_p1.find("TOTAL")+5:])[0]\
                            .replace(".", ";").replace(",", ".").replace(";", ""))

        return darf