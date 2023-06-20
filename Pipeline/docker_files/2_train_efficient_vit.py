from torchvision.datasets import ImageFolder
from albumentations import Compose, RandomBrightnessContrast, \
    HorizontalFlip, FancyPCA, HueSaturationValue, OneOf, ToGray, \
    ShiftScaleRotate, ImageCompression, PadIfNeeded, GaussNoise, GaussianBlur, Rotate
from albu import IsotropicResize
import cv2
from sklearn.utils.class_weight import compute_class_weight 
from utils import get_method, check_correct, resize, shuffle_dataset, get_n_params
import os
from torch.utils.data import DataLoader
from deepfakes_dataset import DeepFakesDataset
import numpy as np
from PIL import Image
import argparse
import collections
import yaml
from progress.bar import ChargingBar
from efficient_vit import EfficientViT
from torch.optim.lr_scheduler import LambdaLR
import torch
from torch.optim import lr_scheduler
import math
want_transfer_learning = True
model_path = './weight/efficient_vit.pth'
MODELS_PATH = './weight/'
batch_size = 32
num_epochs = 30
patience = 5
train_ratio = 0.8

def main(args):
    #Data Loader
    data_path = args.path
    dataset_imgfloder = ImageFolder(data_path)
    dataset_data_path = [Image.open(i[0]) for i in dataset_imgfloder.imgs]
    dataset_data_label = [i[1] for i in dataset_imgfloder.imgs]
    dataset_len = len(dataset_data_path)
    dataset_len = math.floor(dataset_len*train_ratio)
    train_data_path = dataset_data_path[:dataset_len]
    train_data_label = dataset_data_label[:dataset_len]
    train_counters = collections.Counter(train_data_label)

    val_data_path = dataset_data_path[dataset_len:]
    val_data_label = dataset_data_label[dataset_len:]


    class_weights = train_counters[0] / train_counters[1]
    print("Weights", class_weights)
    loss_fn = torch.nn.BCEWithLogitsLoss(pos_weight=torch.tensor([class_weights]))


    train_dataset = DeepFakesDataset(np.asarray(train_data_path, dtype=object), np.asarray(train_data_label), 224)
    val_dataset = DeepFakesDataset(np.asarray(val_data_path, dtype=object), np.asarray(val_data_label), 224 , mode='validation')

    dl = DataLoader(train_dataset, batch_size=batch_size, shuffle=True, sampler=None,
                                    batch_sampler=None, num_workers=1, collate_fn=None,
                                    pin_memory=False, drop_last=False, timeout=0,
                                    worker_init_fn=None, prefetch_factor=2,
                                    persistent_workers=False)

    val_dl = DataLoader(val_dataset, batch_size=batch_size, shuffle=True, sampler=None,
                                    batch_sampler=None, num_workers=1, collate_fn=None,
                                    pin_memory=False, drop_last=False, timeout=0,
                                    worker_init_fn=None, prefetch_factor=2,
                                    persistent_workers=False)

    config_path = "./architecture.yaml"
    with open(config_path, 'r') as ymlfile:
        config = yaml.safe_load(ymlfile)
        
    model = EfficientViT(config=config, channels=1280, selected_efficient_net = 0)

    optimizer = torch.optim.SGD(model.parameters(), lr=config['training']['lr'], weight_decay=config['training']['weight-decay'])
    scheduler = lr_scheduler.StepLR(optimizer, step_size=config['training']['step-size'], gamma=config['training']['gamma'])
    starting_epoch = 0

    print("Model Parameters:", get_n_params(model))

    if want_transfer_learning:
        model.load_state_dict(torch.load(model_path))


    model = model.cuda()
    counter = 0
    not_improved_loss = 0
    previous_loss = math.inf
    for t in range(starting_epoch, num_epochs):
        model.train()
        if not_improved_loss == patience:
            break
        counter = 0
        total_loss = 0
        total_val_loss = 0
        
        
        bar = ChargingBar('EPOCH #' + str(t), max=(len(dl)*config['training']['bs'])+len(val_dl))
        train_correct = 0
        positive = 0
        negative = 0
        for index, (images, labels) in enumerate(dl):
            images = np.transpose(images, (0, 3, 1, 2))
            labels = labels.unsqueeze(1)
            images = images.cuda()
            
            y_pred = model(images)
            y_pred = y_pred.cpu()
            
            loss = loss_fn(y_pred.float(), labels.float())
        
            corrects, positive_class, negative_class = check_correct(y_pred, labels)  
            train_correct += corrects
            positive += positive_class
            negative += negative_class
            optimizer.zero_grad()
            
            loss.backward()
            
            optimizer.step()
            counter += 1
            total_loss += round(loss.item(), 2)
            
            if index%1200 == 0: # Intermediate metrics print
                print("\nLoss: ", total_loss/counter, "Accuracy: ",train_correct/(counter*config['training']['bs']) ,"Train 0s: ", negative, "Train 1s:", positive)


            for i in range(config['training']['bs']):
                bar.next()

        val_correct = 0
        val_positive = 0
        val_negative = 0
        val_counter = 0
        train_correct /= len(train_dataset)
        total_loss /= counter
        model.eval()
        for index, (val_images, val_labels) in enumerate(val_dl):

            val_images = np.transpose(val_images, (0, 3, 1, 2))
            
            val_images = val_images.cuda()
            val_labels = val_labels.unsqueeze(1)
            val_pred = model(val_images)
            val_pred = val_pred.cpu()
            val_loss = loss_fn(val_pred.float(), val_labels.float())
            total_val_loss += round(val_loss.item(), 2)
            corrects, positive_class, negative_class = check_correct(val_pred, val_labels)
            val_correct += corrects
            val_positive += positive_class
            val_counter += 1
            val_negative += negative_class
            bar.next()
            
        scheduler.step()
        bar.finish()
            

        total_val_loss /= val_counter
        val_correct /= len(val_dataset)
        if previous_loss <= total_val_loss:
            print("Validation loss did not improved")
            not_improved_loss += 1
        else:
            not_improved_loss = 0
        
        previous_loss = total_val_loss
        print("#" + str(t) + "/" + str(num_epochs) + " loss:" +
            str(total_loss) + " accuracy:" + str(train_correct) +" val_loss:" + str(total_val_loss) + " val_accuracy:" + str(val_correct) + " val_los:" + str(val_negative))

        if not os.path.exists(MODELS_PATH):
            os.makedirs(MODELS_PATH)
        torch.save(model.state_dict(), os.path.join(MODELS_PATH,  "efficientnetB"+str("0")+"_checkpoint" + str(t) + "_.pth"))


if __name__ == '__main__':
    # argparse 객체 생성
    parser = argparse.ArgumentParser(description='Video Frame Extraction')

    # 입력받을 명령 줄 인수 추가
    parser.add_argument('--path', type=str, help='video_path')

    # 명령 줄 인수 파싱
    args = parser.parse_args()

    # main 함수 호출
    main(args)