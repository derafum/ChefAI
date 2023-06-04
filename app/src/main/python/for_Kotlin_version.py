import os
import requests
import json




def check(qrraw):
    token = "20253.Umk1V1xWs9M87DoWY"
    url = 'https://proverkacheka.com/api/v1/check/get'
    data = {'token': token,'qrraw': qrraw}
    last_response = requests.post(url, data=data)
    data = json.loads(last_response.text)
    names = [item['name'].lower() for item in data['data']['json']['items']]
    list2 = ['томат', 'огурц', 'помидор', 'молоко', 'персик', 'апельсин', 'морковь', 'мандарин', 'перец', 'баклажан', 'картофель', 'картошк', 'перец', 'яйц', 'яблок', 'чеснок', 'лимон', 'репчатый лук', 'банан', 'груш', 'цукин', 'капуст', 'клубника', 'куриц', 'курин', 'свинин', 'вишн', 'виноград', 'лук', 'гриб', 'говядин']

    new_list = []

    for item in names:
        for product in list2:
            if product in item:
                new_list.append(product)
                break

    return new_list

