import cv2
import argparse
from facenet_pytorch.models.mtcnn import MTCNN
import torch
from PIL import Image
import sh
import os

#setting init.
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
detector = MTCNN(margin=0, thresholds=[0.85, 0.95, 0.95], device=device)

def read_file_name(video_path):
    file_list = os.listdir(video_path)
    return file_list

def save_frames(video_path, output_folder, istrue):
    # 비디오 파일 열기
    video_name = video_path.split('/')[-1].split('.')[0]
    video = cv2.VideoCapture(video_path)

    # 비디오 정보 가져오기
    frame_count = int(video.get(cv2.CAP_PROP_FRAME_COUNT))
    fps = video.get(cv2.CAP_PROP_FPS)

    # 프레임 단위로 비디오 읽기
    success, image = video.read()
    count = 0

    #프레임을 저장할 디렉토리를 생성
    folder_path = f"{output_folder}/{istrue}/"
    try:
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
    except OSError:
        print ('Error: Creating directory. ' +  folder_path)

    while success:
        # 현재 프레임을 사진으로 저장
        frame_path = f"{output_folder}/{istrue}/{video_name}-frame_{count}.jpg"
        cv2.imwrite(frame_path, image)
        frame = Image.open(frame_path).convert('RGB')
        # boxes = detector.detect(frame,landmarks=False)
        # if boxes[0] is not None:
        #     boxes = boxes[0][0]
        #     xmin, ymin, xmax, ymax = boxes[0],boxes[1],boxes[2],boxes[3]
        #     cropped_image = frame.crop((xmin,ymin,xmax,ymax))
        #     cropped_image.save(frame_path)
        # else:  
        #     sh.rm(frame_path)
            
        # 다음 프레임 읽기
        success, image = video.read()
        count += 1

        # 진행 상황 출력
        print(f"Saving frame {count}/{frame_count}")

    # 비디오 파일 닫기
    video.release()

    print("Frames saved successfully! video_name : " + video_name)


def main(args):
    video_path = args.video # "영상 파일 경로.mp4"
    output_folder = args.output
    file_list_true = read_file_name(video_path+"/True")
    file_list_false = read_file_name(video_path+"/False")
    
    # 프레임 단위로 사진 저장a
    for video_name in file_list_true:
        save_frames(video_path+"/True/"+video_name, output_folder, True)
    for video_name in file_list_false:
        save_frames(video_path+"/False/"+video_name, output_folder, False)


if __name__ == '__main__':
    # argparse 객체 생성
    parser = argparse.ArgumentParser(description='Video Frame Extraction')

    # 입력받을 명령 줄 인수 추가
    parser.add_argument('--video', type=str, help='video_path')
    parser.add_argument('--output', type=str, help='output_save_path')

    # 명령 줄 인수 파싱
    args = parser.parse_args()

    # main 함수 호출
    main(args)
