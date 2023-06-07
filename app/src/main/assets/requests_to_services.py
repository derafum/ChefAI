import json
import requests
import base64

from pprint import pprint

# =========================обращение к модели распознавания изображений============================

# Кодируем изображение в формат base64
image_jpg = 'IMAGE.jpg'
image_base64 = base64.b64encode(open(image_jpg, 'rb').read()).decode('utf-8')

response = requests.post('https://detect.roboflow.com/-object-detection-pukbl/3?api_key=hzA1SfCPcpXoK4L5LAKe',
                         json=image_base64)

# Выводим результат запроса
print(json.loads(response.text))

# =====================обращение к сервису получения информации по чеку============================

token = '20269.DDqUwXE3jHFbumFYw'
url = 'https://proverkacheka.com/api/v1/check/get'

qrfile = open(image_jpg, 'rb')
response_photo = requests.post(url, data={'token': token}, files={'qrfile': qrfile})
pprint(json.loads(response_photo.text))

qrraw = '20230531T1757&s=559.89&fn=9960440302156794&i=73600&fp=385665697&n=1'
response_qrraw = requests.post(url, data={'token': token, 'qrraw': qrraw})
pprint(json.loads(response_qrraw.text))
