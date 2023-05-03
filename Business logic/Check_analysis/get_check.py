import requests
import json


class ScanCheck:
    """
        Класс для работы с чеком.
        ...
        Атрибуты
        --------
        url : str
            ссылка на proverkacheka
        token : str
            персональный токен
        last_responce : None
            последний полученный чек
        Методы
        ------
        get_json(qrraw: str) -> dict:
            Получаем чек в формате json

        save_check_to_json(filename:str = "info_check.json"):
            Сохраняем данные чека в формате json
        """

    def __init__(self, token):
        self.url = 'https://proverkacheka.com/api/v1/check/get'
        self.token = token
        self.last_response = None

    def get_json(self, qrraw: str) -> dict:
        """

        Args:
            qrraw:

        Returns:

        """
        data = {'token': self.token,
                'qrraw': qrraw}
        self.last_response = requests.post(self.url, data=data)
        return json.loads(self.last_response.text)

    def save_check_to_json(self, filename: str = "info_check.json"):
        """Сохраняем данные чека в формате json"""
        assert self.last_response is not None, "No"
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(json.loads(self.last_response.text), f, ensure_ascii=False, indent=2)
