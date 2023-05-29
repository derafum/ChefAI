import io
from PIL import Image
from roboflow.models.object_detection import ObjectDetectionModel


def main(img):
    # image_data = open('test_img.jpg', mode='rb').read() # test
    image_data = img
    model = Model(api_key=api_key, id_=id_, version=version)
    prediction = model.get_predict(image_data)
    return prediction


class Model(ObjectDetectionModel):
    def __init__(self, api_key, id_, version):
        ObjectDetectionModel.__init__(self, api_key=api_key, id=id_, version=version)

    def get_predict(self, img_bytes, confidence=1, overlap=100, hosted=False):
        img_path = Image.open(io.BytesIO(img_bytes))
        prediction = self.predict(image_path=img_path, confidence=confidence, overlap=overlap, hosted=hosted).json()
        classes = []

        for result in prediction['predictions']:
            if not result['class'] in classes:
                classes.append(result['class'])
        return classes




"""if __name__ == '__main__':
    main()"""

api_key = "hzA1SfCPcpXoK4L5LAKe"  # gitignore
id_ = "mortuus-stellaris-raxs6/-object-detection-pukbl/3"  # gitignore
version = 3  # gitignore