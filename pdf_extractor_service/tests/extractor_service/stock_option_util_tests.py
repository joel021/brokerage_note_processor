import unittest
from datetime import datetime
from extractor_service.stock_option_util import Options


class OptionsTests(unittest.TestCase):


    def __init__(self, methodName='runTest'):
        super().__init__(methodName)

    def test_expiration_date(self):

        option_util = Options()
        expiration_date = option_util.expiration_date({
            "name": "VALEC884",
            "date": "08-02-2023",
            "type": Options.CALL
        })

        expetecd = datetime.strptime("2023-05-19 00:00:00", "%Y-%m-%d %H:%M:%S")
        self.assertEquals(expetecd, expiration_date)


    def test_option_letter(self):

        option_util = Options()
        letter = option_util.option_letter("VALEC884")
        expected = "C"
        self.assertEquals(expected, letter)

    def test_active_name(self):

        option_util = Options()
        name = option_util.active_name("VALEC884")
        expected = "VALE3"

        self.assertEquals(expected, name)

if __name__ == '__main__':
    unittest.main()
