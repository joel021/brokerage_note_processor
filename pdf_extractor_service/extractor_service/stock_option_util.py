import datetime
import re

from pandas import date_range, to_datetime

from extractor_service.constants import ACTIVES

class Options():

    CALL = "CALL"
    PUT = "PUT"

    def __init__(self):
        self.call_m = [chr(ord("A") + i) for i in range(0, 12)]
        self.put_m = [chr(ord("A") + i) for i in range(12, 24)]

    def get_type(self, option_name:str) -> str:
        active_name = self.active_name(option_name)
        l = len(active_name)
        c = option_name[l-1:l]

        if c in self.call_m:
            return self.CALL
        
        return self.PUT

    def active_name(self, option_name):

        for active_name in ACTIVES.keys():
            if active_name[0:-1] in option_name or "L"+active_name[0:-2] in option_name:
                return active_name

        raise Exception(f"No active found to match with the current option: {option_name}")

    def expiration_date(self, option:dict) -> datetime.date:
        """
        :param actives: list of the actives of stock price
        :param option: dict or pandas row with name, type and date
        :return:
        """
        
        option_letter = self.option_letter(option['name'])

        if option['type'] == "CALL":
            month = self.call_m.index(option_letter)+1
        else:
            month = self.put_m.index(option_letter)+1

        try:
            option_date = to_datetime(option['date'], format='%d-%m-%Y')
        except:
            option_date = to_datetime(option['date'])

        year = option_date.year
        if month < option_date.month:
            year += 1

        return date_range(option_date, f'{year}-{month}-{28}', freq='WOM-3FRI')[-1]

    def option_letter(self, option_name:str):

        results = re.findall(r"[A-Z]{1}[0-9]{1}[0-9]*", option_name)
        if len(results) > 0:
            return results[0][0]

        return None
