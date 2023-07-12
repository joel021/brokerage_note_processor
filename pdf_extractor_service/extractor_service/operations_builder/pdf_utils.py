import pypdfium2 as pdfium
from os.path import isfile
import logging

logger = logging.getLogger(__name__)

def retrieve_pages(pdf_uri: str, password: str = None):
    errors = []
    if not isfile(pdf_uri):
        logger.critical(f"{pdf_uri} not found.")
        errors.append("Arquivo não salvo ou deletado.")
        return errors, []

    pdf_file = None

    try:
        pdf_file = open(pdf_uri, "rb")
        pdf = pdfium.PdfDocument(pdf_file, password, autoclose=True)
    except:

        if pdf_file:
            pdf_file.close()

        pdf_file = open(pdf_uri, "rb")

        try:
            pdf = pdfium.PdfDocument(pdf_file, autoclose=True)
        except Exception as e:
            errors.append("Erro ao abrir o PDF. A senha pode estar errada.")
            logger.critical(f"{pdf_uri} can not be opened: "+str(e))
            pdf_file.close()
            return errors, []

    pages = []
    for page in pdf:
        textpage = page.get_textpage()
        pages.append(str(textpage.get_text_range()))
        textpage.close()
        page.close()

    pdf.close()
    pdf_file.close()

    return errors, pages
