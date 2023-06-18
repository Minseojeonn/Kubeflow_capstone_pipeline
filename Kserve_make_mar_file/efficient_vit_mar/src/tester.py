from efficient_vit import EfficientViT
from albumentations import Compose, PadIfNeeded
from albu import IsotropicResize
import cv2
import yaml
import os
import numpy as np
from facenet_pytorch.models.mtcnn import MTCNN
from PIL import Image
import torch

device = "cuda"
with open('./architecture.yaml', 'r') as ymlfile:
    config = yaml.safe_load(ymlfile)
detector = MTCNN(margin=0, thresholds=[0.85, 0.95, 0.95], device=device).to(device)
model = EfficientViT(config=config, channels=1280, selected_efficient_net = 0).to(device)
model.load_state_dict(torch.load('./efficient_vit.pth')) 
model.eval()  

def create_base_transform(size):
        return Compose([
            IsotropicResize(max_side=size, interpolation_down=cv2.INTER_AREA, interpolation_up=cv2.INTER_CUBIC),
            PadIfNeeded(min_height=size, min_width=size, border_mode=cv2.BORDER_CONSTANT),
        ])

transform = create_base_transform(224)
images = []

channels = 1280 #efficientnet 0   
imgstring = Image.open("./temp2.png").convert('RGB')       
boxes = detector.detect(imgstring,landmarks=False)
boxes = boxes[0][0]
xmin, ymin, xmax, ymax = boxes[0],boxes[1],boxes[2],boxes[3]
imgstring = imgstring.crop((xmin,ymin,xmax,ymax))
imgstring.save('./1.png')
img = transform(image=cv2.imread('./1.png'))['image']

images.append(img)
images = torch.tensor(np.asarray(images))
images = np.transpose(images, (0, 3, 1, 2))
images=images.to(device).float()
pred = model(images)



