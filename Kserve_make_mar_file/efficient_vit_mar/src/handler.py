from efficient_vit import EfficientViT
from albumentations import Compose, PadIfNeeded
from albu import IsotropicResize
import cv2
import yaml
import os
from ts.torch_handler.base_handler import BaseHandler
import numpy as np
from facenet_pytorch.models.mtcnn import MTCNN
from PIL import Image
import torch as torch
import io
import base64


class efficientNet_handler(BaseHandler):  
    """  
    A custom model handler implementation.    
    """  
    
    
    def __init__(self):
        self._context = None  
        self.initialized = False  
        self.model = None  
        self.device = None  
        self.detector = None
        

    def initialize(self, context):  
        """  
        Invoke by torchserve for loading a model
        :param context: context contains model server system properties        
        :return:        
        """  
        #  load the model        
        self.manifest = context.manifest  
        properties = context.system_properties  
        model_dir = properties.get("model_dir")  
        self.device = torch.device("cuda:" + str(properties.get("gpu_id")) if torch.cuda.is_available() else "cpu")  
        with open('./architecture.yaml', 'r') as ymlfile:
            config = yaml.safe_load(ymlfile)
        

        # Read model serialize/pt file  
        serialized_file = self.manifest['model']['serializedFile']  
        model_pt_path = os.path.join(model_dir, serialized_file)  
        if not os.path.isfile(model_pt_path):  
            raise RuntimeError("Missing the model.pt file")  
        self.detector = MTCNN(margin=0, thresholds=[0.85, 0.95, 0.95], device=self.device).to(self.device)
        self.model = EfficientViT(config=config, channels=1280, selected_efficient_net = 0).to(self.device) 
        self.model.load_state_dict(torch.load(model_pt_path, map_location=self.device))  
        self.model.eval()  
        self.initialized = True  

    def create_base_transform(self,size):
        return Compose([
            IsotropicResize(max_side=size, interpolation_down=cv2.INTER_AREA, interpolation_up=cv2.INTER_CUBIC),
            PadIfNeeded(min_height=size, min_width=size, border_mode=cv2.BORDER_CONSTANT),
        ])
    
    def base64_to_img(self,base64_data):
        img = base64_data.encode("utf-8")
        img = base64.b64decode(img)
        return img
    
    def handle(self, data, context):  
        """  
        Invoke by TorchServe for prediction request.
        Do pre-processing of data, prediction using model and postprocessing of prediciton output
        :param data: Input data for prediction        
        :param context: Initial context contains model server system properties.        
        :return: prediction output        
        """ 
        transform = self.create_base_transform(224)       
        images = []
        print(data)
        body = data[0]['body']
        channels = 1280 #efficientnet 0
        
        for data in body:
            byte_image = self.base64_to_img(data)
            img = Image.open(io.BytesIO(byte_image)).convert('RGB')       
            boxes = self.detector.detect(img,landmarks=False)
            if boxes[0] is not None:
                boxes = boxes[0][0]
                xmin, ymin, xmax, ymax = boxes[0],boxes[1],boxes[2],boxes[3]
                img = img.crop((xmin,ymin,xmax,ymax))
                img.save('./temp.png')
                img = transform(image=cv2.imread('./temp.png'))['image']
                images.append(img)
        
        
        
        
        images = torch.tensor(np.asarray(images))
        images = np.transpose(images, (0, 3, 1, 2)).to(self.device).float()
        pred = self.model(images)
        result = torch.sigmoid(pred).tolist()
        return result