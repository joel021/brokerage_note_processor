from .operations_extractor import OperationsExtractor

class ReportsBuilder:

    def __init__(self) -> None:
        self.operations = []

        self.reports = OperationsExtractor()
        self.reports.operations = self.operations


"""
    def operations_from_pdf_path(self, path_uri: str) -> pd.DataFrame:
        folders = [path_uri]
        while folders:

            folder = folders.pop(0)
            self.operations_from_files(folder)
            sub_folders_uri = []

            for sub_folder in next(os.walk(folder))[1]:
                sub_folders_uri.append(f"{folder}/{sub_folder}")

            folders.extend(sub_folders_uri)

        return pd.DataFrame(self.operations)

    def operations_from_files(self, path_uri: str, password: str) -> list:
        for pdf_uri in glob(f"{path_uri}/*.pdf"):
            lower_name = os.path.split(pdf_uri)[1].lower()

            if "rico" in lower_name:
                self.reports.from_pdf(pdf_uri, RICO_PASS, "RICO")
            elif "cm" in lower_name:
                self.reports.from_pdf(pdf_uri, CM_CAPITAL_PASS, "CM")

        return self.operations
"""