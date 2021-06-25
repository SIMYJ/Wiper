import os
import cv2
from flask import jsonify
import numpy as np

import numpy as np
import flask
from flask import request, render_template
from flask import send_file
import io

from werkzeug.utils import secure_filename
from werkzeug.datastructures import ImmutableMultiDict
from werkzeug.datastructures import FileStorage

import inpainting_model.GPU_CPU.test as inpainting

app = flask.Flask(__name__)


# 파일 업로드
@app.route('/todos', methods=['POST'])
def upload_file():
    if request.method == 'POST':
        print('Post!!')

        # Reiceve file
        originImg = request.files['file01']    # origin
        maskImg = request.files['file02']    # mask
        print('image : {}'.format(originImg))
        print('image : {}'.format(maskImg))

        # Create file
        originImgName = secure_filename(originImg.filename)
        maskImgName = secure_filename(maskImg.filename)
        print('filename01 : {}'.format(originImgName))
        print('filename02 : {}'.format(maskImgName))

        # Save file
        originImg.save("inpainting_model/samples/testset/1.png")
        maskImg.save("inpainting_model/samples/maskset/1.png")

        # Change mask image
        temp = cv2.imread("inpainting_model/samples/maskset/1.png")
        mask = np.where(temp > 0, 0, 255)
        cv2.imwrite("inpainting_model/samples/maskset/1.png", mask)

        # Image inpainting
        result = inpainting.inpainting_main()

        os.remove("inpainting_model/samples/testset/1.png")
        os.remove("inpainting_model/samples/maskset/1.png")
        os.remove("inpainting_model/GPU_CPU/results/1.png")
        
        print('Send!!')
        
        # Send file
        return result


        


# 실행에서 메인 쓰레드인 경우, 먼저 모델을 불러온 뒤 서버를 시작합니다.
if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)
   
